package cz.incad.kramerius.rest.api.k5.client.item;

import static cz.incad.kramerius.rest.api.k5.client.utils.JSONUtils.link;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
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
import cz.incad.kramerius.rest.api.k5.client.utils.ChildrenNodeProcessor;
import cz.incad.kramerius.rest.api.k5.client.utils.JSONUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
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
 * @author pavels
 *
 */
@Path("/v5.0/item")
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
				if (!PIDSupport.isComposedPID(pid)) {
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
				} else 	throw new PIDNotFound("cannot find stream "+dsid);
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
			if (!PIDSupport.isComposedPID(pid)) {
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
			if (PIDSupport.isComposedPID(pid)) {
				JSONArray jsonArray = new JSONArray();
				List<String> children = solrChildren(pid);

				for (String p : children) {
	    			String repPid = p.replace("/", "");
	    			// vrchni ma odkaz sam na sebe
	    			if (repPid.equals(pid)) continue;
					String uri = UriBuilder.fromResource(ItemResource.class).path("{pid}/children").build(pid).toString();
					JSONObject jsonObject = JSONUtils.pidAndModelDesc(repPid, fedoraAccess, uri.toString(), this.decoratorsAggregate, uri);
					jsonArray.add(jsonObject);
	    		}
				return Response.ok().entity(jsonArray.toString()).build();
			} else {
				return Response.ok().entity(new JSONArray().toString()).build();
			}
		}catch(IOException ex) {
			LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
            return Response.ok().entity("{}").build();
//		} catch (ProcessSubtreeException e) {
//			LOGGER.log(Level.SEVERE,e.getMessage(),e);
//            return Response.ok().entity("{}").build();
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
			LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
			return Response.ok().entity("{}").build();
		} catch (ProcessSubtreeException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(),e);
			return Response.ok().entity("{}").build();
		}
    }

	private JSON siblings(String pid, ObjectPidsPath onePath)
			throws ProcessSubtreeException, IOException {

		List<String> children = new ArrayList<String>();
		if (onePath.getLength() >= 2) {
			String[] pth = onePath.getPathFromRootToLeaf();
			children = solrChildren(pth[pth.length-2]);
//			fedoraAccess.processSubtree(pth[pth.length-2], ch);
//			children = ch.getChildren();
		} else {
			children.add(pid);
		}
		JSONObject object = new JSONObject();
		JSONArray pathArray = new JSONArray();
		for (String p : onePath.getPathFromRootToLeaf()) {
			String uriString = UriBuilder.fromResource(ItemResource.class).path("{pid}/siblings").build(pid).toString();
			JSONObject jsonObject = JSONUtils.pidAndModelDesc(p, fedoraAccess, "siblings",this.decoratorsAggregate,uriString);
			pathArray.add(jsonObject);
		}
		object.put("path", pathArray);
		JSONArray jsonArray = new JSONArray();
		for (String p : children) {
			String uriString = UriBuilder.fromResource(ItemResource.class).path("{pid}/siblings").build(pid).toString();
			JSONObject jsonObject = JSONUtils.pidAndModelDesc(p, fedoraAccess,"siblings",this.decoratorsAggregate, uriString);
			jsonObject.put("selected", p.equals(pid));
			jsonArray.add(jsonObject);
		}
		object.put("siblings", jsonArray);
		return object;
	}

	
	
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
        		if (PIDSupport.isComposedPID(pid)) {
        			
					JSONObject jsonObject = new JSONObject();	
					String uriString = basicURL(pid);
					JSONUtils.pidAndModelDesc(pid, jsonObject, this.fedoraAccess, uriString, this.decoratorsAggregate, null);

					return Response.ok().entity(jsonObject.toString()).build();
        			
        		} else {
        			try {
						PIDParser pidParser = new PIDParser(pid);
						pidParser.objectPid();
						
						JSONObject jsonObject = new JSONObject();	
						
						String uriString = basicURL(pid);
						JSONUtils.pidAndModelDesc(pid, jsonObject, this.fedoraAccess, uriString, this.decoratorsAggregate, null);
						

						return Response.ok().entity(jsonObject.toString()).build();
					} catch (IllegalArgumentException e) {
						throw new GenericApplicationException(e.getMessage());
					} catch (UriBuilderException e) {
						throw new GenericApplicationException(e.getMessage());
					} catch (LexerException e) {
						throw new GenericApplicationException(e.getMessage());
					}
        			
        		}
        	} else {
        		throw new PIDNotFound("pid not found '"+pid+"'");
        	}
    	} catch(IOException e) {
    		throw new PIDNotFound("pid not found '"+pid+"'");
		}
    }


	/**
	 * Basic URL
	 * @param pid
	 * @return
	 */
	public static String basicURL(String pid) {
		String uriString = UriBuilder.fromResource(ItemResource.class).path("{pid}").build(pid).toString();
		return uriString;
	}


	
	
	private List<String> solrChildren(String pid) throws IOException {
		List<String> ll = new ArrayList<String>();
		int rows = 10000;
		int size = 1; // 1 for the first iteration
		int offset = 0;
		while(offset < size)  {
			// request
			Document resp = this.solrAccess.request("q=parent_pid:\""+pid+"\"&rows="+rows+"&start"+offset);
			Element resultelm = XMLUtils.findElement(resp.getDocumentElement(), "result");
			// define size
			size = Integer.parseInt(resultelm.getAttribute("numFound"));
			List<Element> elms = XMLUtils.getElements(resultelm, new XMLUtils.ElementsFilter() {
				@Override
				public boolean acceptElement(Element element) {
					if (element.getNodeName().equals("doc")) {
						return true;
					} else return false;
				}
			});
			for (Element docelm : elms) {
				ll.add(SOLRUtils.value(docelm, "PID",String.class));
			}
			offset = offset  + rows;
		}
		return ll;
	}

}
