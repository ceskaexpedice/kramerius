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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Variant;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.node.ObjectNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import biz.sourcecode.base64Coder.Base64Coder;

import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.DCConent;
import cz.incad.kramerius.document.model.utils.DCContentUtils;
import cz.incad.kramerius.document.model.utils.DescriptionUtils;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.rest.api.utils.ExceptionJSONObjectUtils;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.service.ReplicationService;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.XMLUtils;

/**
 * API endpoint for replications
 * @author pavels
 */
@Path("/replication/{pid}")
public class ReplicationsResource {

    @Inject
    ReplicationService replicationService;

    @Inject
    ResourceBundleService resourceBundleService;
    
    @Inject
    Provider<Locale> localesProvider;
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    @Inject
    SolrAccess solrAccess;

    @Inject
    IsActionAllowed isActionAllowed;

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
                if (this.fedoraAccess.getDC(pid) != null) {
                    Map<String, List<DCConent>> dcs = DCContentUtils.getDCS(fedoraAccess, solrAccess, Arrays.asList(pid));
                    List<DCConent> list = dcs.get(pid);
                    DCConent dcConent = DCConent.collectFirstWin(list);
                    String appURL = ApplicationURL.applicationURL(this.requestProvider.get());
                    if (!appURL.endsWith("/")) appURL += "/";

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("identifiers", JSONArray.fromObject(dcConent.getIdentifiers()));
                    jsonObj.put("publishers", JSONArray.fromObject(dcConent.getPublishers()));
                    jsonObj.put("creators", JSONArray.fromObject(dcConent.getCreators()));
                    jsonObj.put("title", dcConent.getTitle());
                    jsonObj.put("type", dcConent.getType());
                    jsonObj.put("date", dcConent.getDate());
                    jsonObj.put("handle", appURL+"handle/"+pid);

                    return Response.ok().entity(jsonObj).build();
                } else throw new ObjectNotFound(ExceptionJSONObjectUtils.fromMessage("cannot find pid '"+pid+"'").toString());
            }  else throw new ActionNotAllowed(ExceptionJSONObjectUtils.fromMessage("action is not allowed").toString());
        } catch(FileNotFoundException e) {
            throw new ObjectNotFound("cannot find pid '"+pid+"'");
        } catch (IOException e) {
            throw new ReplicateException(e);
        }
    }


    boolean checkPermission(String pid) throws IOException {
        ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
        for (ObjectPidsPath pth : paths) {
            if (this.isActionAllowed.isActionAllowed(SecuredActions.EXPORT_K4_REPLICATIONS.getFormalName(), pid, null, pth)) return true;
        }
        if (paths.length == 0) {
            ObjectPidsPath path = new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid());
            if (this.isActionAllowed.isActionAllowed(SecuredActions.EXPORT_K4_REPLICATIONS.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, path)) return true;
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
    public StreamingOutput prepareExport(@PathParam("pid") String pid) throws ReplicateException {
        try {
            if (checkPermission(pid)) {
                if (this.fedoraAccess.getRelsExt(pid) != null) {
                    // raw generate to request writer
                    List<String> pidList = replicationService.prepareExport(pid);
                    // cannot use JSON object -> too big data
                    return new PIDListStreamOutput(pidList);
                } else throw new ObjectNotFound(ExceptionJSONObjectUtils.fromMessage("cannot find pid '"+pid+"'").toString());
            }  else throw new ActionNotAllowed(ExceptionJSONObjectUtils.fromMessage("action is not allowed").toString());
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
    public Response getExportedFOXML(@PathParam("pid")String pid) throws ReplicateException, UnsupportedEncodingException {
        try {
            if (checkPermission(pid)) {
                // musi se vejit do pameti
                byte[] bytes = replicationService.getExportedFOXML(pid);
                return Response.ok().entity(XMLUtils.parseDocument(new ByteArrayInputStream(bytes), true)).build();
            }  else throw new ActionNotAllowed(ExceptionJSONObjectUtils.fromMessage("action is not allowed").toString());
        } catch(FileNotFoundException e) {
            throw new ObjectNotFound(ExceptionJSONObjectUtils.fromMessage("cannot find pid '"+pid+"'").toString());
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
    public Response getExportedJSONFOXML(@PathParam("pid")String pid) throws ReplicateException, UnsupportedEncodingException {
        try {
            if (checkPermission(pid)) {
                // musi se vejit do pameti
                byte[] bytes = replicationService.getExportedFOXML(pid);
                char[] encoded = Base64Coder.encode(bytes);
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("raw", new String(encoded));
                return Response.ok().entity(jsonObj).build();
            }  else throw new ActionNotAllowed(ExceptionJSONObjectUtils.fromMessage("action is not allowed").toString());
        } catch(FileNotFoundException e) {
            throw new ObjectNotFound(ExceptionJSONObjectUtils.fromMessage("cannot find pid '"+pid+"'").toString());
        } catch (IOException e) {
            throw new ReplicateException(e);
        }
    }
}