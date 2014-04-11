package cz.incad.kramerius.rest.api.k5.client.item;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.JSONDecoratorsAggregate;
import cz.incad.kramerius.rest.api.k5.client.item.exceptions.PIDNotFound;
import cz.incad.kramerius.rest.api.k5.client.utils.JSONUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRDecoratorUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import cz.incad.kramerius.utils.solr.SolrUtils;

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

    @GET
    @Path("{pid}/streams/{dsid}")
    public Response stream(@PathParam("pid") String pid,
            @PathParam("dsid") String dsid) {
        try {
            if (!FedoraUtils.FEDORA_INTERNAL_STREAMS.contains(dsid)) {
                if (!PIDSupport.isComposedPID(pid)) {
                    String mimeTypeForStream = this.fedoraAccess
                            .getMimeTypeForStream(pid, dsid);
                    final InputStream is = this.fedoraAccess.getDataStream(pid,
                            dsid);
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
        }
    }

    @GET
    @Path("{pid}/streams")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response streams(@PathParam("pid") String pid) {
        try {
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
            return Response.ok().entity(jsonObject).build();
        } catch (IOException e) {
            throw new PIDNotFound(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/children")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response children(@PathParam("pid") String pid) {
        try {
            if (!PIDSupport.isComposedPID(pid)) {
                JSONArray jsonArray = new JSONArray();
                List<String> children = solrChildren(pid);

                for (String p : children) {
                    String repPid = p.replace("/", "");
                    // vrchni ma odkaz sam na sebe
                    if (repPid.equals(pid))
                        continue;
                    String uri = UriBuilder.fromResource(ItemResource.class)
                            .path("{pid}/children").build(pid).toString();
                    JSONObject jsonObject = JSONUtils.pidAndModelDesc(repPid,
                            fedoraAccess, uri.toString(),
                            this.decoratorsAggregate, uri);
                    jsonArray.add(jsonObject);
                }
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
            children = solrChildren(parentPid);
            // fedoraAccess.processSubtree(pth[pth.length-2], ch);
            // children = ch.getChildren();
        } else {
            children.add(pid);
        }
        JSONObject object = new JSONObject();
        JSONArray pathArray = new JSONArray();
        for (String p : onePath.getPathFromRootToLeaf()) {
            String uriString = UriBuilder.fromResource(ItemResource.class)
                    .path("{pid}/siblings").build(pid).toString();
            p = PIDSupport.convertToK4Type(p);
            JSONObject jsonObject = JSONUtils.pidAndModelDesc(p, fedoraAccess,
                    uriString, this.decoratorsAggregate, uriString);
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
            JSONObject jsonObject = JSONUtils.pidAndModelDesc(p, fedoraAccess,
                    uriString, this.decoratorsAggregate, uriString);

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

    @GET
    @Path("{pid}")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response basic(@PathParam("pid") String pid) {
        try {
            if (pid != null) {
                if (PIDSupport.isComposedPID(pid)) {

                    JSONObject jsonObject = new JSONObject();
                    String uriString = basicURL(pid);
                    JSONUtils.pidAndModelDesc(pid, jsonObject,
                            this.fedoraAccess, uriString,
                            this.decoratorsAggregate, null);

                    return Response.ok().entity(jsonObject.toString()).build();
                } else {
                    try {
                        PIDParser pidParser = new PIDParser(pid);
                        pidParser.objectPid();

                        JSONObject jsonObject = new JSONObject();

                        String uriString = basicURL(pid);
                        JSONUtils.pidAndModelDesc(pid, jsonObject,
                                this.fedoraAccess, uriString,
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

    
    private List<String> solrChildren(String parentPid) throws IOException {
        //Collections.sort(list, c);
        
        List<Map<String, String>> ll = new ArrayList<Map<String, String>>();
        int rows = 10000;
        int size = 1; // 1 for the first iteration
        int offset = 0;
        while (offset < size) {
            // request
            Document resp = this.solrAccess.request("q=parent_pid:\"" + parentPid
                    + "\"&rows=" + rows + "&start" + offset);
            Element resultelm = XMLUtils.findElement(resp.getDocumentElement(),
                    "result");
            // define size
            size = Integer.parseInt(resultelm.getAttribute("numFound"));
            List<Element> elms = XMLUtils.getElements(resultelm,
                    new XMLUtils.ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element element) {
                            if (element.getNodeName().equals("doc")) {
                                return true;
                            } else
                                return false;
                        }
                    });
            
            for (Element docelm : elms) {
                String docpid = SOLRUtils.value(docelm, "PID", String.class);
                if (docpid.equals(parentPid)) continue;
                Map<String, String> m = new HashMap<String, String>();
                m.put("pid", docpid);
                m.put("index", relsExtIndex(parentPid, docelm));
                ll.add(m);
                //ll.add(SOLRUtils.value(docelm, "PID", String.class));
            }
            offset = offset + rows;
        }
        
        
        Collections.sort(ll, new Comparator<Map<String, String>>() {

            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                Integer i1 = new Integer(o1.get("index"));
                Integer i2 = new Integer(o2.get("index"));
                return i1.compareTo(i2);
            }

        });
        
        List<String> values = new ArrayList<String>();
        for (Map<String, String> m : ll) { values.add(m.get("pid"));}
        return values;
    }

    /**
     * Finds correct rels ext position
     * @param parentPid 
     * @param docelm
     * @return
     */
    private String relsExtIndex(String parentPid, Element docelm) {
        List<Integer> docindexes =  SOLRUtils.array(docelm, "rels_ext_index", Integer.class);
        List<String> parentPids = SOLRUtils.array(docelm, "parent_pid", String.class);
        int index = 0;
        for (int i = 0, length = parentPids.size(); i < length; i++) {
            if (parentPids.get(i).endsWith(parentPid)) {
                index =  i;
                break;
            }
        }
        return ""+docindexes.get(index);
    }

}
