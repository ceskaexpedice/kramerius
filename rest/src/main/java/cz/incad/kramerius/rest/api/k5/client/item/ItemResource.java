package cz.incad.kramerius.rest.api.k5.client.item;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.rest.api.k5.client.item.context.ItemTreeRender;
import cz.incad.kramerius.rest.api.k5.client.item.context.TreeAggregate;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.DecoratorsAggregate;
import cz.incad.kramerius.rest.api.k5.client.item.display.DisplayType;
import cz.incad.kramerius.rest.api.k5.client.item.display.DisplayTypeAggregate;
import cz.incad.kramerius.rest.api.k5.client.item.exceptions.PIDNotFound;
import cz.incad.kramerius.rest.api.k5.client.item.metadata.Metadata;
import cz.incad.kramerius.rest.api.k5.client.item.metadata.MetadataAggregate;
import cz.incad.kramerius.rest.api.k5.client.utils.ChildrenNodeProcessor;
import cz.incad.kramerius.rest.api.k5.client.utils.JSONUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.JSONUtils.JSONElementTree;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Path("/k5/item")
public class ItemResource {

    public static final Logger LOGGER = Logger.getLogger(ItemResource.class.getName());

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    SolrAccess solrAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    MetadataAggregate metadataAggregate;

    @Inject
    DisplayTypeAggregate displayTypeAggregate;

    @Inject
    DecoratorsAggregate decoratorsAggregate;

    @GET
    @Path("{pid}/mods")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response modsJSON(@PathParam("pid") String pid) {
        try {
            InputStream iStream = fedoraAccess.getDataStream(pid, FedoraUtils.BIBLIO_MODS_STREAM);
            Document document = XMLUtils.parseDocument(iStream, true);
            JSONObject retval = new JSONObject();

            JSONElementTree theTree = JSONUtils.elementTree(document);
            retval.put(theTree.getKey(), theTree.toJSON(retval));

            return Response.ok().entity(retval.toString()).build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.noContent().build();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return Response.noContent().build();
        } catch (SAXException e) {
            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @GET
    @Path("{pid}/mods")
    @Produces(MediaType.APPLICATION_XML)
    public Response modsXML(@PathParam("pid") String pid) {
        try {
            InputStream iStream = fedoraAccess.getDataStream(pid, FedoraUtils.BIBLIO_MODS_STREAM);
            String string = IOUtils.readAsString(iStream, Charset.forName("UTF-8"), true);
            return Response.ok().entity(string).build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @GET
    @Path("{pid}/dc")
    @Produces(MediaType.APPLICATION_XML)
    public Response dcXML(@PathParam("pid") String pid) {
        try {
            InputStream iStream = fedoraAccess.getDataStream(pid, FedoraUtils.DC_STREAM);
            String string = IOUtils.readAsString(iStream, Charset.forName("UTF-8"), true);
            return Response.ok().entity(string).build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @GET
    @Path("{pid}/dc")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response dcJSON(@PathParam("pid") String pid) {
        try {
            InputStream iStream = fedoraAccess.getDataStream(pid, FedoraUtils.DC_STREAM);
            Document document = XMLUtils.parseDocument(iStream, true);
            JSONObject retval = new JSONObject();

            JSONElementTree theTree = JSONUtils.elementTree(document);
            retval.put(theTree.getKey(), theTree.toJSON(retval));

            return Response.ok().entity(retval.toString()).build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.noContent().build();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return Response.noContent().build();
        } catch (SAXException e) {
            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @GET
    @Path("{pid}/children")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response children(@PathParam("pid") String pid) {
        try {
            ChildrenNodeProcessor ch = new ChildrenNodeProcessor();
            fedoraAccess.processSubtree(pid, ch);
            List<String> children = ch.getChildren();
            JSONArray jsonArray = new JSONArray();
            for (String p : children) {
                // metadata decorator
                JSONObject jsonObject = JSONUtils.pidAndModelDesc(p, fedoraAccess, "children", this.decoratorsAggregate);
                jsonArray.add(jsonObject);
            }
            return Response.ok().entity(jsonArray.toString()).build();
        } catch (IOException ex) {
            ex.printStackTrace();
            return Response.ok().entity("{}").build();
        } catch (ProcessSubtreeException e) {
            e.printStackTrace();
            return Response.ok().entity("{}").build();
        }
    }

    @GET
    @Path("{pid}/siblings")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response siblings(@PathParam("pid") String pid) {
        try {
            ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
            JSONArray sibsList = new JSONArray();
            for (ObjectPidsPath onePath : paths) {
                // metadata decorator	
                sibsList.add(siblings(pid, onePath));
            }
            return Response.ok().entity(sibsList.toString()).build();
        } catch (IOException ex) {
            ex.printStackTrace();
            return Response.ok().entity("{}").build();
        } catch (ProcessSubtreeException e) {
            e.printStackTrace();
            return Response.ok().entity("{}").build();
        }
    }

    private JSON siblings(String pid, ObjectPidsPath onePath)
            throws ProcessSubtreeException, IOException {
        List<String> children = new ArrayList<String>();
        if (onePath.getLength() >= 2) {
            String[] pth = onePath.getPathFromRootToLeaf();
            ChildrenNodeProcessor ch = new ChildrenNodeProcessor();
            fedoraAccess.processSubtree(pth[pth.length - 2], ch);
            children = ch.getChildren();
        } else {
            children.add(pid);
        }
        JSONObject object = new JSONObject();
        JSONArray pathArray = new JSONArray();
        for (String p : onePath.getPathFromRootToLeaf()) {
            JSONObject jsonObject = JSONUtils.pidAndModelDesc(p, fedoraAccess, "siblings", this.decoratorsAggregate);
            pathArray.add(jsonObject);
        }
        object.put("path", pathArray);
        JSONArray jsonArray = new JSONArray();
        for (String p : children) {
            JSONObject jsonObject = JSONUtils.pidAndModelDesc(p, fedoraAccess, "siblings", this.decoratorsAggregate);
//			String str = ApplicationURL.applicationURL(this.requestProvider.get()).toString()+"/img?pid="+p+"&stream=IMG_THUMB&action=GETRAW";
//			jsonObject.put("url", str);
            jsonObject.put("selected", p.equals(pid));
            jsonArray.add(jsonObject);
        }
        object.put("siblings", jsonArray);
        return object;
    }

    // TODO 
    @GET
    @Path("{pid}/context")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response context(@PathParam("pid") String pid) {
        try {
            ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
            JSONArray jsonArray = new JSONArray();
            for (ObjectPidsPath ppath : paths) {
                JSONArray subArr = jsonArr(ppath, "context", decoratorsAggregate);
                jsonArray.add(subArr);
            }
            return Response.ok().entity(jsonArray.toString()).build();
        } catch (IOException ex) {
            return Response.ok().entity("{}").build();
        }

    }

    private JSONArray jsonArr(ObjectPidsPath ppath, String context, DecoratorsAggregate decoratorsAggregate) throws IOException {
        JSONArray subArray = new JSONArray();
        String[] pths = ppath.getPathFromRootToLeaf();
        for (String p : pths) {
            JSONObject jsonObject = JSONUtils.pidAndModelDesc(p, this.fedoraAccess, context, decoratorsAggregate);
            // TODO: decorators
            subArray.add(jsonObject);
        }
        return subArray;
    }

    @GET
    @Path("{pid}/display")
    @Produces(MediaType.APPLICATION_JSON)
    public Response display(@PathParam("pid") String pid) {
        HashMap<String, Object> opts = new HashMap<String, Object>();
        DisplayType dtype = this.displayTypeAggregate.getDisplayType(pid, opts);
        if (dtype != null) {
            return Response.ok().entity(dtype.getDisplay(pid, opts).toString()).build();
        } else {
            return Response.ok().entity("{}").build();
        }
    }

    @GET
    @Path("{pid}/full")
    public Response full(@PathParam("pid") String pid) {
        try {
            String suri = ApplicationURL.applicationURL(this.requestProvider.get()) + "/img?pid=" + pid + "&stream=IMG_FULL&action=GETRAW";
            URI uri = new URI(suri);
            return Response.temporaryRedirect(uri).build();
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new PIDNotFound("pid not found '" + pid + "'");
        }
    }

    @GET
    @Path("{pid}/preview")
    public Response preview(@PathParam("pid") String pid) {
        try {
            String suri = ApplicationURL.applicationURL(this.requestProvider.get()) + "/img?pid=" + pid + "&stream=IMG_PREVIEW&action=GETRAW";
            URI uri = new URI(suri);
            return Response.temporaryRedirect(uri).build();
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new PIDNotFound("pid not found '" + pid + "'");
        }
    }

    @GET
    @Path("{pid}/thumb")
    public Response thumb(@PathParam("pid") String pid) {
        try {
            String suri = ApplicationURL.applicationURL(this.requestProvider.get()) + "/img?pid=" + pid + "&stream=IMG_THUMB&action=GETRAW";
            URI uri = new URI(suri);
            return Response.temporaryRedirect(uri).build();
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new PIDNotFound("pid not found '" + pid + "'");
        }
    }

    @GET
    @Path("{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response basic(@PathParam("pid") String pid) {
        try {
            if (pid != null) {
                JSONObject jsonObject = new JSONObject();
                JSONUtils.pidAndModelDesc(pid, jsonObject, this.fedoraAccess, "", this.decoratorsAggregate);
//        		
//        		jsonObject.put("tree", UriBuilder.fromPath("{pid}/tree").build(pid).toString());
//        		jsonObject.put("display", UriBuilder.fromPath("{pid}/display").build(pid).toString());
//        		//details
//        		jsonObject.put("details", details(pid));
//        		
//        		//metadata - extension point
//        		Metadata metadata = this.metadataAggregate.getMetadataCollector(pid);
//        		if (metadata != null) {
//        			jsonObject.put("metadata", metadata.collect(pid));
//        		}
//        		
//        		// display type & options
//        		DisplayType dtype = this.displayTypeAggregate.getDisplayType(pid);
//        		if (dtype != null) {
//        			jsonObject.put("display", dtype.getDisplay(pid));
//        		}
//        		
//        		String appURL = ApplicationURL.applicationURL(this.requestProvider.get());
//        		
                return Response.ok().entity(jsonObject.toString()).build();
            } else {
                throw new PIDNotFound("pid not found '" + pid + "'");
            }
        } catch (IOException e) {
            throw new PIDNotFound("pid not found '" + pid + "'");
        }
    }

}
