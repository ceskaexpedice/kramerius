package cz.incad.kramerius.rest.api.k5.client.item;

import static cz.incad.kramerius.rest.api.k5.client.utils.JSONUtils.link;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import cz.incad.kramerius.rest.api.k5.client.JSONDecoratorsAggregate;
import cz.incad.kramerius.rest.api.k5.client.item.exceptions.PIDNotFound;
import cz.incad.kramerius.rest.api.k5.client.utils.ChildrenNodeProcessor;
import cz.incad.kramerius.rest.api.k5.client.utils.JSONUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;

@Path("/v5.0/k5/item")
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
	JSONDecoratorsAggregate decoratorsAggregate;



	@GET
	@Path("{pid}/streams/{dsid}")
    public Response stream(@PathParam("pid")String pid, @PathParam("dsid")String dsid) {
		try {
			if (!FedoraUtils.FEDORA_INTERNAL_STREAMS.contains(dsid)) {
				String mimeTypeForStream = this.fedoraAccess.getMimeTypeForStream(pid, dsid);
				final InputStream is = this.fedoraAccess.getDataStream(pid, dsid);
				StreamingOutput stream = new StreamingOutput() {
			        public void write(OutputStream output) throws IOException, WebApplicationException {
			            try {
			            	IOUtils.copyStreams(is, output);
			            } catch (Exception e) {
			                throw new WebApplicationException(e);
			            }
			        }
			    };
				return Response.ok().entity(stream).type(mimeTypeForStream).build();
			} else {
				throw new PIDNotFound("cannot find stream "+dsid);
			}
		} catch (IOException e) {
			throw new PIDNotFound(e.getMessage());
		}
	}

	
	@GET
	@Path("{pid}/streams")
    @Produces(MediaType.APPLICATION_JSON)
    public Response streams(@PathParam("pid")String pid) {
		try {
			JSONObject jsonObject = new JSONObject();
			Document datastreams = this.fedoraAccess.getFedoraDataStreamsListAsDocument(pid);
			Element documentElement = datastreams.getDocumentElement();
			List<Element> elms = XMLUtils.getElements(documentElement);
			for (Element e : elms) {
				JSONObject streamObj  = new JSONObject(); 
				String dsiId = e.getAttribute("dsid");

				if (FedoraUtils.FEDORA_INTERNAL_STREAMS.contains(dsiId)) continue;
				
				String label = e.getAttribute("label");
				streamObj.put("label", label);
				
				String mimeType = e.getAttribute("mimeType");
				streamObj.put("mimeType", mimeType);

				jsonObject.put(dsiId, streamObj);
			}

			return Response.ok().entity(jsonObject).build();
		} catch (IOException e) {
			throw new PIDNotFound(e.getMessage());
		}
	}
	
	

	

	@GET
	@Path("{pid}/children")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response children(@PathParam("pid")String pid) {
		try {
			ChildrenNodeProcessor ch = new ChildrenNodeProcessor();	
			fedoraAccess.processSubtree(pid, ch);
			List<String> children = ch.getChildren();
			JSONArray jsonArray = new JSONArray();
			for (String p : children) {
				// metadata decorator
				String uri = UriBuilder.fromPath("{pid}/children").build(p).toString();
				JSONObject jsonObject = JSONUtils.pidAndModelDesc(p, fedoraAccess, uri.toString(), this.decoratorsAggregate, uri);
				jsonArray.add(jsonObject);
			}
			return Response.ok().entity(jsonArray.toString()).build();
		}catch(IOException ex) {
			ex.printStackTrace();
            return Response.ok().entity("{}").build();
		} catch (ProcessSubtreeException e) {
			e.printStackTrace();
			return Response.ok().entity("{}").build();
		}
    }

	@GET
	@Path("{pid}/siblings")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response siblings(@PathParam("pid")String pid) {
		try {
			ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
			JSONArray sibsList = new JSONArray();
			for (ObjectPidsPath onePath : paths) {
				// metadata decorator	
				sibsList.add(siblings(pid, onePath));
			}
			return Response.ok().entity(sibsList.toString()).build();
		}catch(IOException ex) {
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
			fedoraAccess.processSubtree(pth[pth.length-2], ch);
			children = ch.getChildren();
		} else {
			children.add(pid);
		}
		JSONObject object = new JSONObject();
		JSONArray pathArray = new JSONArray();
		for (String p : onePath.getPathFromRootToLeaf()) {
			String uriString = UriBuilder.fromPath("{pid}").build(p).toString();
			JSONObject jsonObject = JSONUtils.pidAndModelDesc(p, fedoraAccess, "siblings",this.decoratorsAggregate,uriString);
			pathArray.add(jsonObject);
		}
		object.put("path", pathArray);
		JSONArray jsonArray = new JSONArray();
		for (String p : children) {
			String uriString = UriBuilder.fromPath("{pid}").build(p).toString();
			JSONObject jsonObject = JSONUtils.pidAndModelDesc(p, fedoraAccess,"siblings",this.decoratorsAggregate, uriString);
//			String str = ApplicationURL.applicationURL(this.requestProvider.get()).toString()+"/img?pid="+p+"&stream=IMG_THUMB&action=GETRAW";
//			jsonObject.put("url", str);
			jsonObject.put("selected", p.equals(pid));
			jsonArray.add(jsonObject);
		}
		object.put("siblings", jsonArray);
		return object;
	}

	
//	// TODO 
//	@GET
//	@Path("{pid}/context")
//    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
//    public Response context(@PathParam("pid")String pid) {
//		try {
//			ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
//			JSONArray jsonArray = new JSONArray();
//			for (ObjectPidsPath ppath : paths) {
//				JSONArray subArr = jsonArr(ppath, "context", decoratorsAggregate);
//				jsonArray.add(subArr);
//			}
//			return Response.ok().entity(jsonArray.toString()).build();
//		}catch(IOException ex) {
//            return Response.ok().entity("{}").build();
//		}
//
//    }
//

//	private JSONArray jsonArr( ObjectPidsPath ppath,String context, DecoratorsAggregate decoratorsAggregate) throws IOException {
//		JSONArray subArray = new JSONArray();
//		String[] pths = ppath.getPathFromRootToLeaf();
//		for (String p : pths) {
//			String uriString = UriBuilder.fromPath("{pid}").build(p).toString();
//			JSONObject jsonObject = JSONUtils.pidAndModelDesc(p, this.fedoraAccess, context, decoratorsAggregate, uriString);
//			// TODO: decorators
//			subArray.add(jsonObject);
//		}
//		return subArray;
//	}

	


	
	
	@GET
	@Path("{pid}/full")
	public Response full(@PathParam("pid")String pid) {
		try {
			String suri = ApplicationURL.applicationURL(this.requestProvider.get())+"/img?pid="+pid+"&stream=IMG_FULL&action=GETRAW";
			URI uri = new URI(suri);
			return Response.temporaryRedirect(uri).build();
		} catch (URISyntaxException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(),e);
    		throw new PIDNotFound("pid not found '"+pid+"'");
		} 
	}

	@GET
	@Path("{pid}/preview")
    public Response preview(@PathParam("pid")String pid) {
		try {
			String suri = ApplicationURL.applicationURL(this.requestProvider.get())+"/img?pid="+pid+"&stream=IMG_PREVIEW&action=GETRAW";
			URI uri = new URI(suri);
			return Response.temporaryRedirect(uri).build();
		} catch (URISyntaxException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(),e);
    		throw new PIDNotFound("pid not found '"+pid+"'");
		} 
	}

	@GET
	@Path("{pid}/thumb")
    public Response thumb(@PathParam("pid")String pid) {
		try {
			String suri = ApplicationURL.applicationURL(this.requestProvider.get())+"/img?pid="+pid+"&stream=IMG_THUMB&action=GETRAW";
			URI uri = new URI(suri);
			return Response.temporaryRedirect(uri).build();
		} catch (URISyntaxException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(),e);
    		throw new PIDNotFound("pid not found '"+pid+"'");
		} 
	}
	
	
	@GET
	@Path("{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response basic(@PathParam("pid")String pid) {
    	try {
        	if (pid != null) {
        		JSONObject jsonObject = new JSONObject();	
        		
        		
        		JSONUtils.pidAndModelDesc(pid, jsonObject, this.fedoraAccess, "", this.decoratorsAggregate, null);
        		
        		Document datastreams = this.fedoraAccess.getFedoraDataStreamsListAsDocument(pid);
        		Element documentElement = datastreams.getDocumentElement();
        		Set<String> datastreamsEn = new HashSet<String>();
        		List<Element> elms = XMLUtils.getElements(documentElement);
        		for (Element e : elms) {
					datastreamsEn.add(e.getAttribute("dsid"));
				}

        		return Response.ok().entity(jsonObject.toString()).build();
        	} else {
        		throw new PIDNotFound("pid not found '"+pid+"'");
        	}
    	} catch(IOException e) {
    		throw new PIDNotFound("pid not found '"+pid+"'");
		}
    }


	public static void main(String[] args) {
		URI t = UriBuilder.fromResource(ItemResource.class).path("{pid}").build("xxx");
		System.out.println(t);
		

	}
}
