package cz.incad.kramerius.rest.apiNew.cdk.v70.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.ParserConfigurationException;

import cz.incad.kramerius.security.SecuredAkubraRepository;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import biz.sourcecode.base64Coder.Base64Coder;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.service.ReplicationService;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.replication.FormatType;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
//import cz.incad.kramerius.virtualcollections.Collection;
//import cz.incad.kramerius.virtualcollections.CollectionUtils;

public class SOLRResource {

    @Inject
    ReplicationService replicationService;

    @Inject
    ResourceBundleService resourceBundleService;

    @Inject
    Provider<Locale> localesProvider;

    @Inject
    SecuredAkubraRepository akubraRepository;

    @Inject
    @javax.inject.Named("new-index")
    SolrAccess solrAccess;

    @Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    Provider<User> userProvider;
    



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
//    @GET
//    @Path("{pid}/solrxml")
//    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getExportedSolrXML(@PathParam("pid") String pid)
            throws ReplicateException, UnsupportedEncodingException {
        try {
            Document solrDoc = this.solrAccess.getSolrDataByPid(pid);
            return Response.ok().entity(solrDoc).build();
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
//    @GET
//    @Path("{pid}/{page}/solrxml")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getCombinedPidExportedSolrXML(@PathParam("pid") String pid,@PathParam("page") String page)
            throws ReplicateException, UnsupportedEncodingException {
        try {
            Document solrDoc = this.solrAccess.getSolrDataByPid(pid+"/"+page);
            return Response.ok().entity(solrDoc).build();
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
//    @GET
//    @Path("{pid}/foxml")
//    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getExportedFOXML(@PathParam("pid") String pid,
            @QueryParam("collection") String collection)
            throws ReplicateException, UnsupportedEncodingException {
        try {
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
//    @GET
//    @Path("{pid}/foxml")
//    @Produces(MediaType.APPLICATION_JSON)
    public Response getExportedJSONFOXML(@PathParam("pid") String pid,
            @QueryParam("collection") String collection)
            throws ReplicateException, UnsupportedEncodingException {
        try {
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

    public Response batchedFOXL(@QueryParam("pids") String stringPids, @QueryParam("collection") String collection)  throws ReplicateException, IOException {
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
        } catch (JSONException e) {
            throw new ReplicateException(e);
        } catch (RuntimeException e)  {
            if (e.getCause() != null) throw new ReplicateException(e.getCause());
            else throw new RuntimeException(e);
        }
        return Response.ok().entity(bos.toByteArray()).build();
    }


//    @GET
////    @Path("batch/solrxmls")
////    @Produces("application/zip")
//    public Response batchedSOLRXML(@QueryParam("pids") String stringPids) throws ReplicateException {
//        try {
//            String[] pids = stringPids.split(",");
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            try(ZipOutputStream zipOutputStream = new ZipOutputStream(bos)) {
//                Arrays.stream(pids).forEach(pid-> {
//                    try {
//
//                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                        zipOutputStream.putNextEntry(new ZipEntry(pid));
//                        Document solrDoc = this.solrAccess.getSolrDataByPid(pid);
//                        XMLUtils.print(solrDoc, stream);
//
//                        byte[] bytes = stream.toByteArray();
//                        zipOutputStream.write(bytes, 0, bytes.length);
//                        zipOutputStream.closeEntry();
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    } catch (TransformerException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//                return Response.ok().entity(bos.toByteArray()).build();
//        } catch (RuntimeException e) {
//            if (e.getCause() != null) throw new ReplicateException(e.getCause());
//             else throw new ReplicateException(e);
//        }
//    }


//    @GET
//    @Path("solr/select")
//    @Consumes({ MediaType.APPLICATION_XML })
//    @Produces({ MediaType.APPLICATION_XML + ";charset=utf-8" })
    public Response selectXML(@Context UriInfo uriInfo) throws IOException {
        return solrResponse(uriInfo,"xml");
    }

//    @GET
//    @Path("solr/select")
//    @Consumes({ MediaType.APPLICATION_JSON })
//    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response selectJSON(@Context UriInfo uriInfo) throws IOException {
        return solrResponse(uriInfo,"json");
    }

    private Response solrResponse(@Context UriInfo uriInfo, String format) throws IOException {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        StringBuilder builder = new StringBuilder();
        Set<String> keys = queryParameters.keySet();
        for (String k : keys) {
            for (String v : queryParameters.get(k)) {
                String value = URLEncoder.encode(v, "UTF-8");
                builder.append(k + "=" + value);
                builder.append("&");
            }
        }
        InputStream istream = this.solrAccess.requestWithSelectReturningInputStream(builder.toString(), format, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copyStreams(istream, bos);
        String rawString = new String(bos.toByteArray(), "UTF-8");
        return Response.ok().type(MediaType.APPLICATION_XML+ ";charset=utf-8").entity(rawString).build();
    }

}
