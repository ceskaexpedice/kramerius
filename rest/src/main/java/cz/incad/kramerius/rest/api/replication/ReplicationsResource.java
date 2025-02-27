/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.rest.api.replication;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.pdfbox.io.IOUtils;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import biz.sourcecode.base64Coder.Base64Coder;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.DCConent;
import cz.incad.kramerius.document.model.utils.DCContentUtils;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.service.ReplicationService;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.replication.FormatType;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.XMLUtils;

/**
 * API endpoint for replications
 * @author pavels
 */
@Path("/v4.6/replication/{pid}")
public class ReplicationsResource {

    @Inject
    ReplicationService replicationService;

    @Inject
    ResourceBundleService resourceBundleService;
    
    @Inject
    Provider<Locale> localesProvider;
    
    /* TODO AK_NEW
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

     */
    @Inject
    AkubraRepository akubraRepository;

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    @Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    Provider<User> userProvider;
    
    /**
     * Returns DC content
     * @param pid PID of object
     * @return DC content
     * @throws ReplicateException throw if an error has been occured
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    public Response getExportedDescription(@PathParam("pid") String pid) throws ReplicateException {
        try {
            if (checkPermission(pid)) {
                if (akubraRepository.datastreamExists(pid, KnownDatastreams.BIBLIO_DC)) {
                    Map<String, List<DCConent>> dcs = DCContentUtils.getDCS(akubraRepository, solrAccess, Arrays.asList(pid));
                    List<DCConent> list = dcs.get(pid);
                    DCConent dcConent = DCConent.collectFirstWin(list);
                    String appURL = ApplicationURL.applicationURL(this.requestProvider.get());
                    if (!appURL.endsWith("/")) appURL += "/";

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("identifiers", new JSONArray(dcConent.getIdentifiers()));
                    jsonObj.put("publishers", new JSONArray(dcConent.getPublishers()));
                    jsonObj.put("creators", new JSONArray(dcConent.getCreators()));
                    jsonObj.put("title", dcConent.getTitle());
                    jsonObj.put("type", dcConent.getType());
                    jsonObj.put("date", dcConent.getDate());
                    jsonObj.put("handle", appURL+"handle/"+pid);

                    return Response.ok().entity(jsonObj.toString()).build();
                } else throw new ObjectNotFound("cannot find pid '"+pid+"'");
            }  else throw new ActionNotAllowed("action is not allowed");
        } catch(FileNotFoundException e) {
            throw new ObjectNotFound("cannot find pid '"+pid+"'");
        } catch (IOException e) {
            throw new ReplicateException(e);
        } catch (JSONException e) {
            throw new ReplicateException(e);
        }
    }


    boolean checkPermission(String pid) throws IOException {
        ObjectPidsPath[] paths = this.solrAccess.getPidPaths(pid);
        for (ObjectPidsPath pth : paths) {
            if (this.rightsResolver.isActionAllowed(SecuredActions.A_EXPORT_REPLICATIONS.getFormalName(), pid, null, pth).flag()) return true;
        }
        if (paths.length == 0) {
            ObjectPidsPath path = new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid());
            if (this.rightsResolver.isActionAllowed(SecuredActions.A_EXPORT_REPLICATIONS.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, path).flag()) return true;
        }
        return false;
    }


    
    /**
     * Prepare all pids for replication
     * @param pid Requested object
     * @return collection of pids needs to be replicated
     * @throws ReplicateException Cannot prepare list
     */
    @GET
    @Path("tree")
    @Produces(MediaType.APPLICATION_JSON)
    public StreamingOutput prepareExport(@PathParam("pid") String pid,@QueryParam("replicateCollections") @DefaultValue("false")String replicateCollections) throws ReplicateException {
        try {
            ObjectPidsPath[] paths = this.solrAccess.getPidPaths(pid);
            if (checkPermission(pid)) {
                if (akubraRepository.datastreamExists(pid, KnownDatastreams.RELS_EXT)) {
                    // raw generate to request writer
                	boolean collectionFlag = Boolean.parseBoolean(replicateCollections);
                	List<String> pidList = replicationService.prepareExport(pid,collectionFlag);
                    // cannot use JSON object -> too big data
                    return new PIDListStreamOutput(pidList, Arrays.asList(paths));
                } else throw new ObjectNotFound("cannot find pid '"+pid+"'");
            }  else throw new ActionNotAllowed("action is not allowed");
        } catch(FileNotFoundException e) {
            throw new ObjectNotFound("cannot find pid '"+pid+"'");
        } catch (IOException e) {
            throw new ReplicateException(e);
        }
    }

    /**
     * Returns exported FOXML in xml format
     * @param pid PID of object 
     * @return FOXML as application xml
     * @throws ReplicateException An error has been occured
     * @throws UnsupportedEncodingException  UTF-8 is not supported
     */
    @GET
    @Path("foxml")
    @Produces(MediaType.APPLICATION_XML+";charset=utf-8")
    public Response getExportedFOXML(@PathParam("pid")String pid,
    		@QueryParam("replicateCollections") @DefaultValue("false")String replicateCollections) throws ReplicateException, UnsupportedEncodingException {
        try {
            if (checkPermission(pid)) {
            	boolean collectionFlag = Boolean.parseBoolean(replicateCollections);
            	if (collectionFlag) {
                	byte[] bytes = replicationService.getExportedFOXML(pid, FormatType.EXTERNALREFERENCES);
                    return Response.ok().entity(XMLUtils.parseDocument(new ByteArrayInputStream(bytes), true)).build();
            	} else{
                	byte[] bytes = replicationService.getExportedFOXML(pid, FormatType.EXTERNALREFERENCESANDREMOVECOLS);
                    return Response.ok().entity(XMLUtils.parseDocument(new ByteArrayInputStream(bytes), true)).build();
            	}
            }  else throw new ActionNotAllowed("action is not allowed");
        } catch(FileNotFoundException e) {
            throw new ObjectNotFound("cannot find pid '"+pid+"'");
        } catch (IOException e) {
            throw new ReplicateException(e);
        } catch (ParserConfigurationException e) {
            throw new ReplicateException(e);
        } catch (SAXException e) {
            throw new ReplicateException(e);
        }
    }

    /**
     * Returns exported FOXML enveloped in JSON object
     * @param pid PID of object
     * @return FOXML as JSON
     * @throws ReplicateException Cannot export JSON
     * @throws UnsupportedEncodingException UTF-8 is not supported
     */
    @GET
    @Path("foxml")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExportedJSONFOXML(@PathParam("pid")String pid,@QueryParam("replicateCollections") @DefaultValue("false")String replicateCollections) throws ReplicateException, UnsupportedEncodingException {
        try {
            if (checkPermission(pid)) {
                // musi se vejit do pameti
            	boolean collectionFlag = Boolean.parseBoolean(replicateCollections);
            	if (collectionFlag) {
                	byte[] bytes = replicationService.getExportedFOXML(pid,FormatType.EXTERNALREFERENCES);
                    char[] encoded = Base64Coder.encode(bytes);
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("raw", new String(encoded));
                    return Response.ok().entity(jsonObj.toString()).build();
            	} else{
                	byte[] bytes = replicationService.getExportedFOXML(pid,FormatType.EXTERNALREFERENCESANDREMOVECOLS);
                    char[] encoded = Base64Coder.encode(bytes);
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("raw", new String(encoded));
                    return Response.ok().entity(jsonObj.toString()).build();
            	}

            }  else throw new ActionNotAllowed("action is not allowed");
        } catch(FileNotFoundException e) {
            throw new ObjectNotFound("cannot find pid '"+pid+"'");
        } catch (IOException e) {
            throw new ReplicateException(e);
        } catch (JSONException e) {
            throw new ReplicateException(e);
        }
    }

    @GET
    @Path("img_original")
    @Produces("image/jp2")
    public Response getOriginalImage(@PathParam("pid") String pid) throws XPathExpressionException, IOException {
        Document doc = akubraRepository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT).asDom(false);
        String tilesUrl = RelsExtUtils.getRelsExtTilesUrl(doc);
        if (tilesUrl == null) return Response.status(Response.Status.NOT_FOUND).build();

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (final CloseableHttpResponse httpResponse = httpclient.execute(new HttpGet(tilesUrl + "/original"))) {

                switch (httpResponse.getStatusLine().getStatusCode()) {
                    case 200:
                        break;
                    case 404:
                        return Response.status(Response.Status.NOT_FOUND).build();
                    default:
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }

                StreamingOutput stream = new StreamingOutput() {
                    @Override
                    public void write(OutputStream output) throws IOException, WebApplicationException {
                        IOUtils.copy(httpResponse.getEntity().getContent(), output);
                        output.flush();
                    }
                };
                return Response.ok(stream).build();
            }
        }
    }
}
