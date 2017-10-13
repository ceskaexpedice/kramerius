package cz.incad.kramerius.rest.api.replication;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import biz.sourcecode.base64Coder.Base64Coder;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.service.ReplicationService;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.replication.FormatType;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.CollectionUtils;
import cz.incad.kramerius.virtualcollections.CollectionsManager;

/**
 * CDK replication resource
 * 
 * @author pavels
 */
@Path("/v4.6/cdk")
public class CDKReplicationsResource {

    public static Logger LOGGER = Logger
            .getLogger(CDKReplicationsResource.class.getName());

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");

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
    
    @Inject
    @Named("fedora")
    CollectionsManager colManager;


    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getVirtualCollections() throws IOException {
        if (checkPermission()) {
            try {
                List<Collection> vcs = this.colManager.getCollections();
                JSONArray jsonArr = new JSONArray();
                for (Collection vc : vcs) {
                    jsonArr.put(CollectionUtils.virtualCollectionTOJSON(vc));
                }
                return Response.ok().entity(jsonArr.toString()).build();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new GenericApplicationException(e.getMessage());
            }
        } else
            throw new ActionNotAllowed("action is not allowed");
    }


    @GET
    @Path("prepare")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response prepare(@QueryParam("date") String date,
            @QueryParam("offset") @DefaultValue("0") String offset,
            @QueryParam("rows") @DefaultValue("100") String rows)
            throws ReplicateException, UnsupportedEncodingException {
        try {
            if (checkPermission()) {
                if (date == null) {
                    date = FORMAT.format(new Date());
                }
                // TODO: permissions
                Document document = this.solrAccess.request(makeRequestURL(
                        date, offset, rows));
                return Response.ok().entity(document).build();
            } else
                throw new ActionNotAllowed("action is not allowed");
        } catch (FileNotFoundException e) {
            throw new ReplicateException(e);
        } catch (IOException e) {
            throw new ReplicateException(e);
        }
    }

    boolean checkPermission() throws IOException {
        ObjectPidsPath path = new ObjectPidsPath(
                SpecialObjects.REPOSITORY.getPid());
        if (this.isActionAllowed.isActionAllowed(
                SecuredActions.EXPORT_CDK_REPLICATIONS.getFormalName(),
                SpecialObjects.REPOSITORY.getPid(), null, path))
            return true;
        return false;
    }

    private String makeRequestURL(String date, String offset, String rows) {
        return "fl=PID,modified_date&sort=modified_date%20asc&q=modified_date:{"
                + date + "%20TO%20NOW}&start=" + offset + "&rows=" + rows;
    }

    @GET
    @Path("{pid}/solrxml")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getExportedSolrXML(@PathParam("pid") String pid)
            throws ReplicateException, UnsupportedEncodingException {
        try {
            if (checkPermission()) {
                Document solrDoc = this.solrAccess.getSolrDataDocument(pid);
                return Response.ok().entity(solrDoc).build();
            } else
                throw new ActionNotAllowed("action is not allowed");
        } catch (FileNotFoundException e) {
            throw new ObjectNotFound("cannot find pid '" + pid + "'");
        } catch (IOException e) {
            throw new ReplicateException(e);
        }
    }

    @GET
    @Path("{pid}/{page}/solrxml")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getCombinedPidExportedSolrXML(@PathParam("pid") String pid,@PathParam("page") String page)
            throws ReplicateException, UnsupportedEncodingException {
        try {
            if (checkPermission()) {
                Document solrDoc = this.solrAccess.getSolrDataDocument(pid+"/"+page);
                return Response.ok().entity(solrDoc).build();
            } else
                throw new ActionNotAllowed("action is not allowed");
        } catch (FileNotFoundException e) {
            throw new ObjectNotFound("cannot find pid '" + pid + "'");
        } catch (IOException e) {
            throw new ReplicateException(e);
        }
    }


    @GET
    @Path("{pid}/foxml")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getExportedFOXML(@PathParam("pid") String pid,
            @QueryParam("collection") String collection)
            throws ReplicateException, UnsupportedEncodingException {
        try {
            if (checkPermission()) {
                // TODO: permissions
                // musi se vejit do pameti
                byte[] bytes = new byte[0];
                if (collection != null) {
                    bytes = replicationService.getExportedFOXML(pid,
                            FormatType.CDK, collection);
                    return Response
                            .ok()
                            .entity(XMLUtils.parseDocument(
                                    new ByteArrayInputStream(bytes), true))
                            .build();
                } else {
                    bytes = replicationService.getExportedFOXML(pid,
                            FormatType.CDK);
                    return Response
                            .ok()
                            .entity(XMLUtils.parseDocument(
                                    new ByteArrayInputStream(bytes), true))
                            .build();
                }
            } else
                throw new ActionNotAllowed("action is not allowed");
        } catch (FileNotFoundException e) {
            throw new ObjectNotFound("cannot find pid '" + pid + "'");
        } catch (IOException e) {
            throw new ReplicateException(e);
        } catch (ParserConfigurationException e) {
            throw new ReplicateException(e);
        } catch (SAXException e) {
            throw new ReplicateException(e);
        }
    }

    @GET
    @Path("{pid}/foxml")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExportedJSONFOXML(@PathParam("pid") String pid,
            @QueryParam("collection") String collection)
            throws ReplicateException, UnsupportedEncodingException {
        try {
            if (checkPermission()) {
                // musi se vejit do pameti
                byte[] bytes = new byte[0];
                if (collection != null) {
                    bytes = replicationService.getExportedFOXML(pid,
                            FormatType.CDK, collection);
                } else {
                    bytes = replicationService.getExportedFOXML(pid,
                            FormatType.CDK);
                }
                char[] encoded = Base64Coder.encode(bytes);
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("raw", new String(encoded));
                return Response.ok().entity(jsonObj.toString()).build();
            } else
                throw new ActionNotAllowed("action is not allowed");
        } catch (FileNotFoundException e) {
            throw new ObjectNotFound("cannot find pid '" + pid + "'");
        } catch (IOException e) {
            throw new ReplicateException(e);
        } catch (JSONException e) {
            throw new ReplicateException(e);
        }
    }

}
