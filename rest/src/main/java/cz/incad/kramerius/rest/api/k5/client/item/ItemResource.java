package cz.incad.kramerius.rest.api.k5.client.item;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.audio.AudioFormat;
import cz.incad.kramerius.audio.AudioStreamForwardUtils;
import cz.incad.kramerius.audio.AudioStreamId;
import cz.incad.kramerius.audio.urlMapping.RepositoryUrlManager;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowedXML;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.JSONDecoratorsAggregate;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.item.exceptions.PIDNotFound;
import cz.incad.kramerius.rest.api.k5.client.item.utils.ItemResourceUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.JSONUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.service.ReplicationService;
import cz.incad.kramerius.service.replication.FormatType;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * Item endpoint
 * 
 * @author pavels
 * 
 */
@Path("/v5.0/item")
public class ItemResource {

    public static final Logger LOGGER = Logger.getLogger(ItemResource.class
            .getName());

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    SolrAccess solrAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;
    
    @Inject
    Provider<HttpServletResponse> responseProvider;
    
    @Inject
    JSONDecoratorsAggregate decoratorsAggregate;

    
    @Inject
    SolrMemoization solrMemoization;

    @Inject
    IsActionAllowed isActionAllowed;

    @Inject
    ReplicationService replicationService;

    @Inject
    Provider<User> userProvider;

    // only for audio streams
    @Inject
    RepositoryUrlManager urlManager;

    
    @GET
    @Path("{pid}/foxml")
    @Produces({ MediaType.APPLICATION_XML + ";charset=utf-8" })
    public Response foxml(@PathParam("pid") String pid) {
        boolean access = false;
        try {
            ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
            for (ObjectPidsPath path : paths) {
                if (this.isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), pid, null, path)) {
                    access = true;
                    break;
                }
            }
            if (access) {
                checkPid(pid);
                byte[] bytes = replicationService.getExportedFOXML(pid, FormatType.IDENTITY);
                final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                StreamingOutput stream = new StreamingOutput() {
                    public void write(OutputStream output)
                            throws IOException, WebApplicationException {
                        try {
                            IOUtils.copyStreams(is, output);
                        } catch (Exception e) {
                            throw new WebApplicationException(e);
                        }
                    }
                };
                return Response.ok().entity(stream).build();
            } else throw new ActionNotAllowedXML("access denied");
        } catch (IOException e) {
            throw new PIDNotFound("cannot foxml for  " + pid);
        } catch (ReplicateException e) {
            throw new PIDNotFound("cannot foxml for  " + pid);
        }
    }


    @HEAD
    @Path("{pid}/streams/{dsid}")
    public Response streamHead(@PathParam("pid") String pid,
            @PathParam("dsid") String dsid) {
        checkPid(pid);
        try {
            checkPid(pid);
            if (!FedoraUtils.FEDORA_INTERNAL_STREAMS.contains(dsid)) {
                if (!PIDSupport.isComposedPID(pid)) {

                    // audio streams - bacause of support rage in headers
                    if (FedoraUtils.AUDIO_STREAMS.contains(dsid)) {
                        String mimeTypeForStream = this.fedoraAccess
                                .getMimeTypeForStream(pid, dsid);

                        ResponseBuilder responseBuilder = Response.ok();
                        responseBuilder = responseBuilder.type(mimeTypeForStream);

                        HttpServletRequest request = this.requestProvider.get();
                        User user = this.userProvider.get();

                        AudioStreamId audioStreamId = new AudioStreamId(pid, AudioFormat.valueOf(dsid));
                        ResponseBuilder builder = AudioStreamForwardUtils.HEAD(audioStreamId, request, responseBuilder, solrAccess, user, this.isActionAllowed, urlManager);
                        return builder.build();
                        
                    } else {
                        String mimeTypeForStream = this.fedoraAccess
                                .getMimeTypeForStream(pid, dsid);

                        class _StreamHeadersObserver implements StreamHeadersObserver {
                            ResponseBuilder respBuilder = null;
                            @Override
                            public void observeHeaderFields(int statusCode,
                                    Map<String, List<String>> headerFields) {
                                respBuilder = Response.status(statusCode);
                                Set<String> keys = headerFields.keySet();
                                for (String k : keys) {
                                    List<String> vals = headerFields.get(k);
                                    for (String val : vals) {
                                        respBuilder.header(k, val);
                                    }
                                }
                            }

                            public ResponseBuilder getBuider() {
                                return this.respBuilder;
                            }
                        }
                        
                        _StreamHeadersObserver observer = new _StreamHeadersObserver();
                        this.fedoraAccess.observeStreamHeaders(pid, dsid,observer);

                        if (observer.getBuider() != null) {
                            return observer.getBuider().type(mimeTypeForStream).build();
                        } else {
                            return Response.ok().type(mimeTypeForStream)
                                    .build();
                        }
                    }
                } else
                    throw new PIDNotFound("cannot find stream " + dsid);
            } else {
                throw new PIDNotFound("cannot find stream " + dsid);
            }
        } catch (IOException e) {
            throw new PIDNotFound(e.getMessage());
        } catch (SecurityException e) {
            throw new ActionNotAllowed(e.getMessage());
        }
        
        
    }    
    
    @GET
    @Path("{pid}/streams/{dsid}")
    public Response stream(@PathParam("pid") String pid,
            @PathParam("dsid") String dsid) {
        try {
            checkPid(pid);
            if (!FedoraUtils.FEDORA_INTERNAL_STREAMS.contains(dsid)) {
                if (!PIDSupport.isComposedPID(pid)) {

                    // audio streams - bacause of support rage in headers
                    if (FedoraUtils.AUDIO_STREAMS.contains(dsid)) {
                        String mimeTypeForStream = this.fedoraAccess
                                .getMimeTypeForStream(pid, dsid);

                        ResponseBuilder responseBuilder = Response.ok();
                        responseBuilder = responseBuilder.type(mimeTypeForStream);

                        HttpServletRequest request = this.requestProvider.get();
                        User user = this.userProvider.get();
                        AudioStreamId audioStreamId = new AudioStreamId(pid, AudioFormat.valueOf(dsid));
                        ResponseBuilder builder = AudioStreamForwardUtils.GET(audioStreamId, request, responseBuilder, solrAccess, user, this.isActionAllowed, urlManager);
                        return builder.build();
                        
                    } else {
                        final InputStream is = this.fedoraAccess.getDataStream(pid,
                                dsid);
                        String mimeTypeForStream = this.fedoraAccess
                                .getMimeTypeForStream(pid, dsid);
                        StreamingOutput stream = new StreamingOutput() {
                            public void write(OutputStream output)
                                    throws IOException, WebApplicationException {
                                try {
                                    IOUtils.copyStreams(is, output);
                                } catch (Exception e) {
                                    throw new WebApplicationException(e);
                                }
                            }
                        };
                        return Response.ok().entity(stream).type(mimeTypeForStream)
                                .build();
                    }
                } else
                    throw new PIDNotFound("cannot find stream " + dsid);
            } else {
                throw new PIDNotFound("cannot find stream " + dsid);
            }
        } catch (IOException e) {
            throw new PIDNotFound(e.getMessage());
        } catch (SecurityException e) {
            throw new ActionNotAllowed(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/streams")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response streams(@PathParam("pid") String pid) {
        try {
            checkPid(pid);
            JSONObject jsonObject = new JSONObject();
            if (!PIDSupport.isComposedPID(pid)) {
                Document datastreams = this.fedoraAccess
                        .getFedoraDataStreamsListAsDocument(pid);
                Element documentElement = datastreams.getDocumentElement();
                List<Element> elms = XMLUtils.getElements(documentElement);
                for (Element e : elms) {
                    JSONObject streamObj = new JSONObject();
                    String dsiId = e.getAttribute("dsid");

                    if (FedoraUtils.FEDORA_INTERNAL_STREAMS.contains(dsiId))
                        continue;

                    String label = e.getAttribute("label");
                    streamObj.put("label", label);

                    String mimeType = e.getAttribute("mimeType");
                    streamObj.put("mimeType", mimeType);

                    jsonObject.put(dsiId, streamObj);
                }
            }
            return Response.ok().entity(jsonObject.toString()).build();
        } catch (IOException e) {
            throw new PIDNotFound(e.getMessage());
        } catch (JSONException e1) {
            throw new GenericApplicationException(e1.getMessage());
        }
    }

    @GET
    @Path("{pid}/children")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response children(@PathParam("pid") String pid) {
        try {
            checkPid(pid);
            if (!PIDSupport.isComposedPID(pid)) {
                JSONArray jsonArray = ItemResourceUtils.decoratedJSONChildren(pid, this.solrAccess, this.solrMemoization, this.decoratorsAggregate);
                return Response.ok().entity(jsonArray.toString()).build();
            } else {
                return Response.ok().entity(new JSONArray().toString()).build();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return Response.ok().entity("{}").build();
        } catch (JSONException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/siblings")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response siblings(@PathParam("pid") String pid) {
        try {
            checkPid(pid);
            ObjectPidsPath[] paths = null;
            if (PIDSupport.isComposedPID(pid)) {
                paths = this.solrAccess.getPath(PIDSupport
                        .convertToSOLRType(pid));
            } else {
                paths = this.solrAccess.getPath(pid);
            }

            JSONArray sibsList = new JSONArray();
            for (ObjectPidsPath onePath : paths) {
                // metadata decorator
                sibsList.put(siblings(pid, onePath));
            }
            return Response.ok().entity(sibsList.toString()).build();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        } catch (ProcessSubtreeException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }
    }

    private JSONObject siblings(String pid, ObjectPidsPath onePath)
            throws ProcessSubtreeException, IOException, JSONException {

        String parentPid = null;
        List<String> children = new ArrayList<String>();
        if (onePath.getLength() >= 2) {
            String[] pth = onePath.getPathFromRootToLeaf();
            parentPid = pth[pth.length - 2];
            
            children = ItemResourceUtils.solrChildrenPids(parentPid, new ArrayList<String>(), this.solrAccess, this.solrMemoization);
        } else {
            children.add(pid);
        }

        JSONObject object = new JSONObject();
        JSONArray pathArray = new JSONArray();
        for (String p : onePath.getPathFromRootToLeaf()) {
            String uriString = UriBuilder.fromResource(ItemResource.class)
                    .path("{pid}/siblings").build(pid).toString();
            p = PIDSupport.convertToK4Type(p);
            JSONObject jsonObject = JSONUtils.pidAndModelDesc(p,
                    uriString,this.solrMemoization, this.decoratorsAggregate, uriString);
            pathArray.put(jsonObject);
        }
        object.put("path", pathArray);
        JSONArray jsonArray = new JSONArray();
        for (String p : children) {
            if (parentPid != null && p.equals(parentPid))
                continue;
            String uriString = UriBuilder.fromResource(ItemResource.class)
                    .path("{pid}/siblings").build(pid).toString();
            p = PIDSupport.convertToK4Type(p);
            JSONObject jsonObject = JSONUtils.pidAndModelDesc(p, uriString, this.solrMemoization, this.decoratorsAggregate, uriString);

            jsonObject.put("selected", p.equals(pid));
            jsonArray.put(jsonObject);
        }
        object.put("siblings", jsonArray);
        return object;
    }

    @GET
    @Path("{pid}/full")
    public Response full(@PathParam("pid") String pid, @QueryParam("asFile")String asFile) {
        try {
            checkPid(pid);
            if (PIDSupport.isComposedPID(pid)) {
                String fpid = PIDSupport.first(pid);
                String page = PIDSupport.rest(pid);
                int rpage = Integer.parseInt(page) - 1;
                if (rpage < 0)
                    rpage = 0;
                String suri = ApplicationURL
                        .applicationURL(this.requestProvider.get())
                        + "/img?pid="
                        + fpid
                        + "&stream=IMG_FULL&action=TRANSCODE&page=" + rpage;
                
                if (StringUtils.isAnyString(asFile)) {
                    suri = suri +"&asFile=true";
                }
                URI uri = new URI(suri);
                
                return Response.temporaryRedirect(uri).build();
            } else {
                
                String suri = ApplicationURL
                        .applicationURL(this.requestProvider.get())
                        + "/img?pid=" + pid + "&stream=IMG_FULL&action=GETRAW";
                
                if (StringUtils.isAnyString(asFile)) {
                    suri = suri +"&asFile=true";
                }

                URI uri = new URI(suri);
                
                return Response.temporaryRedirect(uri).build();
            }
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new PIDNotFound("pid not found '" + pid + "'");
        }
    }

    @GET
    @Path("{pid}/preview")
    public Response preview(@PathParam("pid") String pid) {
        try {
            checkPid(pid);
            if (PIDSupport.isComposedPID(pid)) {
                String fpid = PIDSupport.first(pid);
                String page = PIDSupport.rest(pid);
                int rpage = Integer.parseInt(page) - 1;
                if (rpage < 0)
                    rpage = 0;

                String suri = ApplicationURL
                        .applicationURL(this.requestProvider.get())
                        + "/img?pid="
                        + fpid
                        + "&stream=IMG_PREVIEW&action=TRANSCODE&page=" + rpage;
                URI uri = new URI(suri);
                return Response.temporaryRedirect(uri).build();
            } else {
                String suri = ApplicationURL
                        .applicationURL(this.requestProvider.get())
                        + "/img?pid="
                        + pid
                        + "&stream=IMG_PREVIEW&action=GETRAW";
                URI uri = new URI(suri);
                return Response.temporaryRedirect(uri).build();
            }
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new PIDNotFound("pid not found '" + pid + "'");
        }
    }

    @GET
    @Path("{pid}/thumb")
    public Response thumb(@PathParam("pid") String pid) {
        try {
            checkPid(pid);
            if (PIDSupport.isComposedPID(pid)) {
                String fpid = PIDSupport.first(pid);
                String page = PIDSupport.rest(pid);
                int rpage = Integer.parseInt(page) - 1;
                if (rpage < 0)
                    rpage = 0;

                String suri = ApplicationURL
                        .applicationURL(this.requestProvider.get())
                        + "/img?pid="
                        + fpid
                        + "&stream=IMG_THUMB&action=TRANSCODE&page=" + rpage;
                URI uri = new URI(suri);
                return Response.temporaryRedirect(uri).build();
            } else {
                String suri = ApplicationURL
                        .applicationURL(this.requestProvider.get())
                        + "/img?pid=" + pid + "&stream=IMG_THUMB&action=GETRAW";
                URI uri = new URI(suri);
                return Response.temporaryRedirect(uri).build();
            }
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new PIDNotFound("pid not found '" + pid + "'");
        }
    }

    private void checkPid(String pid) throws PIDNotFound {
        try {
            if (PIDSupport.isComposedPID(pid)) {
                String p = PIDSupport.first(pid);
                if (!this.fedoraAccess.isObjectAvailable(p)) {
                    throw new PIDNotFound("pid not found");
                }
            } else {
                if (!this.fedoraAccess.isObjectAvailable(pid)) {
                    throw new PIDNotFound("pid not found");
                }
            }
        } catch (IOException e) {
            throw new PIDNotFound("pid not found");
        } catch(Exception e) {
            throw new PIDNotFound("error while parsing pid ("+pid+")");
        }
    }
    
    @GET
    @Path("{pid}")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response basic(@PathParam("pid") String pid) {
        try {
            if (pid != null) {
                checkPid(pid);
                if (PIDSupport.isComposedPID(pid)) {

                    JSONObject jsonObject = new JSONObject();
                    String uriString = basicURL(pid);
                    JSONUtils.pidAndModelDesc(pid, jsonObject,uriString, this.solrMemoization,
                            this.decoratorsAggregate, null);

                    return Response.ok().entity(jsonObject.toString()).build();
                } else {
                    try {
                        PIDParser pidParser = new PIDParser(pid);
                        pidParser.objectPid();

                        JSONObject jsonObject = new JSONObject();

                        String uriString = basicURL(pid);
                        JSONUtils.pidAndModelDesc(pid, jsonObject,
                                uriString, this.solrMemoization,
                                this.decoratorsAggregate, null);

                        return Response.ok().entity(jsonObject.toString())
                                .build();
                    } catch (IllegalArgumentException e) {
                        throw new GenericApplicationException(e.getMessage());
                    } catch (UriBuilderException e) {
                        throw new GenericApplicationException(e.getMessage());
                    } catch (LexerException e) {
                        throw new GenericApplicationException(e.getMessage());
                    }
                }
            } else {
                throw new PIDNotFound("pid not found '" + pid + "'");
            }
        } catch (IOException e) {
            throw new PIDNotFound("pid not found '" + pid + "'");
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }
    }

    /**
     * Basic URL
     * 
     * @param pid
     * @return
     */
    public static String basicURL(String pid) {
        String uriString = UriBuilder.fromResource(ItemResource.class)
                .path("{pid}").build(pid).toString();
        return uriString;
    }

}
