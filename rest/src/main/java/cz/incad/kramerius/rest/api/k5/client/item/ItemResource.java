package cz.incad.kramerius.rest.api.k5.client.item;

import com.google.inject.*;
import com.google.inject.name.*;
import cz.incad.kramerius.*;
import cz.incad.kramerius.rest.api.exceptions.*;
import cz.incad.kramerius.rest.api.k5.client.*;
import cz.incad.kramerius.rest.api.k5.client.item.exceptions.*;
import cz.incad.kramerius.rest.api.k5.client.item.utils.*;
import cz.incad.kramerius.rest.api.k5.client.utils.*;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.service.*;
import cz.incad.kramerius.service.replication.*;
import cz.incad.kramerius.utils.*;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.*;
import net.sf.json.*;
import org.w3c.dom.*;

import javax.servlet.http.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

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
    JSONDecoratorsAggregate decoratorsAggregate;

    
    @Inject
    SolrMemoization solrMemoization;

    @Inject
    IsActionAllowed isActionAllowed;

    @Inject
    ReplicationService replicationService;
    
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
    
    @GET
    @Path("{pid}/streams/{dsid}")
    public Response stream(@PathParam("pid") String pid,
            @PathParam("dsid") String dsid) {
        try {
            checkPid(pid);
            if (!FedoraUtils.FEDORA_INTERNAL_STREAMS.contains(dsid)) {
                if (!PIDSupport.isComposedPID(pid)) {
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
                sibsList.add(siblings(pid, onePath));
            }
            return Response.ok().entity(sibsList.toString()).build();

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return Response.ok().entity("{}").build();
        } catch (ProcessSubtreeException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.ok().entity("{}").build();
        }
    }

    private JSON siblings(String pid, ObjectPidsPath onePath)
            throws ProcessSubtreeException, IOException {

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
            pathArray.add(jsonObject);
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
            jsonArray.add(jsonObject);
        }
        object.put("siblings", jsonArray);
        return object;
    }

    @GET
    @Path("{pid}/full")
    public Response full(@PathParam("pid") String pid) {
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
                URI uri = new URI(suri);
                return Response.temporaryRedirect(uri).build();
            } else {
                String suri = ApplicationURL
                        .applicationURL(this.requestProvider.get())
                        + "/img?pid=" + pid + "&stream=IMG_FULL&action=GETRAW";
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
                this.fedoraAccess.getRelsExt(p);
            } else {
                this.fedoraAccess.getRelsExt(pid);
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
