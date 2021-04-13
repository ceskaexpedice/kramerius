package cz.incad.kramerius.rest.api.replication;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import javax.xml.transform.TransformerException;

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

    /**
     * Return virtual collections from resource
     * @return
     * @throws IOException
     */
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

    /**
     * Prepare list of items to be replicated
     * @param date Date from
     * @param offset Offset
     * @param rows  Number of rows
     * @return List of items to be replicated
     * @throws ReplicateException
     * @throws UnsupportedEncodingException
     */
    @GET
    @Path("prepare")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response prepare(@QueryParam("date") String date,
            @QueryParam("offset") @DefaultValue("0") String offset,
            @QueryParam("rows") @DefaultValue("100") String rows)
            throws ReplicateException {
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

    boolean checkPermission() {
        ObjectPidsPath path = new ObjectPidsPath(
                SpecialObjects.REPOSITORY.getPid());
        if (this.isActionAllowed.isActionAllowed(
                SecuredActions.EXPORT_CDK_REPLICATIONS.getFormalName(),
                SpecialObjects.REPOSITORY.getPid(), null, path).flag())
            return true;
        return false;
    }

    private String makeRequestURL(String date, String offset, String rows) {
        return "fl=PID,modified_date&sort=modified_date%20asc&q=modified_date:{"
                + date + "%20TO%20NOW}&start=" + offset + "&rows=" + rows;
    }

    /**
     * Return one solr xml record
     * @param pid PID
     * @return returns solr xml record
     * @throws ReplicateException
     * @throws UnsupportedEncodingException
     */
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

    /**
     * Returns one solr xml record for page (used only in PDF)
     * @param pid Pid of PDF
     * @param page Requested page
     * @return
     * @throws ReplicateException
     * @throws UnsupportedEncodingException
     */
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

    /**
     * Returns foxml for given pid as XML
     * @param pid PID
     * @param collection Collection representing source
     * @return
     * @throws ReplicateException
     * @throws UnsupportedEncodingException
     */
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

    /**
     * Returns foxml for given pid as JSON format
     * @param pid PID
     * @param collection collection represents source
     * @return
     * @throws ReplicateException
     * @throws UnsupportedEncodingException
     */
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


    // Batch support
    // Consider to move it to different endpoint

    /**
     * Returns whole batch of foxml files
     * @param pids Comma separated string of pids
     * @param collection Collection represents source
     * @return
     * @throws ReplicateException
     */
    @GET
    @Path("batch/foxmls")
    @Produces("application/zip")
    public Response batchedFOXL(@QueryParam("pids") String stringPids, @QueryParam("collection") String collection)  throws ReplicateException {
        try {
            if (checkPermission()) {
                String[] pids = stringPids.split(",");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try(ZipOutputStream zipOutputStream = new ZipOutputStream(bos)) {
                    Arrays.stream(pids).forEach(pid-> {
                        try {
                            zipOutputStream.putNextEntry(new ZipEntry(pid));
                            byte[] bytes = new byte[0];
                            if (collection != null) {
                                bytes = replicationService.getExportedFOXML(pid,
                                        FormatType.CDK, collection);
                            } else {
                                bytes = replicationService.getExportedFOXML(pid,
                                        FormatType.CDK);
                            }
                            zipOutputStream.write(bytes, 0, bytes.length);
                            zipOutputStream.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (ReplicateException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (IOException e) {
                    throw new ReplicateException(e);
                }
                return Response.ok().entity(bos.toByteArray()).build();
            } else
                throw new ActionNotAllowed("action is not allowed");
        } catch (JSONException e) {
            throw new ReplicateException(e);
        } catch (RuntimeException e)  {
            if (e.getCause() != null) throw new ReplicateException(e.getCause());
            else throw new RuntimeException(e);
        }
    }


    @GET
    @Path("batch/solrxmls")
    @Produces("application/zip")
    public Response batchedSOLRXML(@QueryParam("pids") String stringPids) throws ReplicateException {
        try {
            if (checkPermission()) {
                String[] pids = stringPids.split(",");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try(ZipOutputStream zipOutputStream = new ZipOutputStream(bos)) {
                    Arrays.stream(pids).forEach(pid-> {
                        try {

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            zipOutputStream.putNextEntry(new ZipEntry(pid));
                            Document solrDoc = this.solrAccess.getSolrDataDocument(pid);
                            XMLUtils.print(solrDoc, stream);

                            byte[] bytes = stream.toByteArray();
                            zipOutputStream.write(bytes, 0, bytes.length);
                            zipOutputStream.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (TransformerException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (IOException e) {
                    throw new ReplicateException(e);
                }
                return Response.ok().entity(bos.toByteArray()).build();
            } else
                throw new ActionNotAllowed("action is not allowed");
        } catch (RuntimeException e) {
            if (e.getCause() != null) throw new ReplicateException(e.getCause());
             else throw new ReplicateException(e);
        }
    }


}
