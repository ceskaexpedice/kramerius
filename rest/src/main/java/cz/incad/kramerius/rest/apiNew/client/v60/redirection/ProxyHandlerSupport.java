package cz.incad.kramerius.rest.apiNew.client.v60.redirection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.AlreadyRegistedPidsException;
import cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.ReharvestItem;
import cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.ReharvestItem.TypeOfReharvset;
import cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public abstract class ProxyHandlerSupport {

	public static final boolean DEBUG = false;
	
	public static final Logger LOGGER = Logger.getLogger(ProxyHandlerSupport.class.getName());

	protected String source;
	protected Client client;
	protected SolrAccess solrAccess;
	protected User user;
	protected String remoteAddr;
	protected Instances instances;
	protected ReharvestManager reharvestManager;

	public ProxyHandlerSupport(ReharvestManager reharvestManager, Instances instances, User user, Client client, SolrAccess solrAccess, String source,
			String remoteAddr) {
	    this.reharvestManager = reharvestManager;
		this.source = source;
		this.client = client;
		this.solrAccess = solrAccess;
		this.user = user;
		this.remoteAddr = remoteAddr;
		this.instances = instances;
	}

	public Response buildRedirectResponse(String url) throws ProxyHandlerException {
		try {
			LOGGER.info(String.format("Redirecting to %s", url));
			return Response.temporaryRedirect(new URL(url).toURI()).build();
		} catch (MalformedURLException | URISyntaxException e) {
			throw new ProxyHandlerException(e);
		}
	}

	/**
	 * Build rewsponse with HEAD method
	 * @param url
	 * @return
	 * @throws ProxyHandlerException
	 */
	public Response buildForwardResponseHEAD(String url) throws ProxyHandlerException {
		WebResource.Builder b = buidFowrardResponse(url);
		ClientResponse clientResponseHead = b.head();
		if (clientResponseHead.getStatus() == 200) {
			return Response.status(200).build();
		} else {
			return Response.status(clientResponseHead.getStatus()).build();
		}
	}

	public ClientResponse forwardedResponse(String url) throws ProxyHandlerException {
		WebResource.Builder b = buidFowrardResponse(url);
		ClientResponse response = b.get(ClientResponse.class);
		if (response.getStatus() == 200) {
			return response;
		} else {
			throw new ProxyHandlerException("Bad response; status code "+response.getStatus());
		}
	}
	
    public Response buildForwardResponseGET(String url) throws ProxyHandlerException {
        return buildForwardResponseGET(url, null, null);
    }
    
    public Response buildForwardResponseGET(String url, String pid) throws ProxyHandlerException {
        return buildForwardResponseGET(url, null, pid);
    }
    

    public Response buildForwardResponseGET(String url, String mimetype, String pid) throws ProxyHandlerException {
		WebResource.Builder b = buidFowrardResponse(url);
		ClientResponse response = b.get(ClientResponse.class);
		if (response.getStatus() == 200) {
		    String responseMimeType = response.getType().toString();
		    InputStream is = response.getEntityInputStream();
			MultivaluedMap<String, String> headers = response.getHeaders();

			StreamingOutput stream = new StreamingOutput() {
				public void write(OutputStream output) throws IOException, WebApplicationException {
					try {
						IOUtils.copy(is, output);
					} catch (Exception e) {
						throw new WebApplicationException(e);
					}
				}
			};
			ResponseBuilder respEntity = null;
			if (mimetype != null) {
                respEntity = Response.status(200).entity(stream).type(mimetype);
			} else if (responseMimeType != null) {
                respEntity = Response.status(200).entity(stream).type(responseMimeType);
			} else {
		        respEntity = Response.status(200).entity(stream);
			}
			
			/* Disable header forward 
			headers.keySet().forEach(key -> {
				List<String> values = headers.get(key);
				values.stream().forEach(val -> {
					respEntity.header(key, val);
				});
			});
            */
			
			return respEntity.build();
		} else {
            // event for reharvest
		    if (response.getStatus() == 404) {
		        deleteTriggeToReharvest(pid);
	        }
			return Response.status(response.getStatus()).build();
		}
	}

    public void deleteTriggeToReharvest(String pid) {
        if (reharvestManager != null && pid != null) {
            
            try {
                Document solrDataByPid = this.solrAccess.getSolrDataByPid(pid);
                Element rootPid = XMLUtils.findElement(solrDataByPid.getDocumentElement(),  new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        if (element.getNodeName().equals("str")) {
                            String fieldName = element.getAttribute("name");
                            return fieldName.equals("root.pid");
                        }
                        return false;
                    }
                });
                
                Element ownPidPath = XMLUtils.findElement(solrDataByPid.getDocumentElement(),  new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        if (element.getNodeName().equals("str")) {
                            String fieldName = element.getAttribute("name");
                            return fieldName.equals("own_pid_path");
                        }
                        return false;
                    }
                });
                
                Element ownParentPid = XMLUtils.findElement(solrDataByPid.getDocumentElement(),  new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        if (element.getNodeName().equals("str")) {
                            String fieldName = element.getAttribute("name");
                            return fieldName.equals("own_parent.pid");
                        }
                        return false;
                    }
                });
                
                
                
                if (rootPid != null && ownPidPath != null && ownParentPid != null) {
                    String pidPath = ownPidPath.getTextContent().trim();
                    String ownParentPidText = ownParentPid.getTextContent().trim();
                    int index = pidPath.indexOf(ownParentPidText);
                    if (index >= 0) {
                        pidPath = pidPath.substring(0, index + ownParentPidText.length()).trim();
                    }
                    try {
                        ReharvestItem reharvestItem = new ReharvestItem(UUID.randomUUID().toString(), "Delete trigger - reharvest from core","open", ownParentPid.getTextContent().trim(), pidPath);
                        reharvestItem.setTypeOfReharvest(TypeOfReharvset.children);
                        reharvestItem.setState("waiting_for_approve");

                        LOGGER.info(String.format("Registering item %s", reharvestItem.toJSON().toString());

                        this.reharvestManager.register(reharvestItem);
                    } catch (DOMException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    } catch (AlreadyRegistedPidsException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                } else {
                    LOGGER.log(Level.SEVERE, "Cannot find root.pid element");
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage());
            }
        } else {
            LOGGER.log(Level.SEVERE,"No reharvest manager or pid ");
        }
    }

	protected void mockSession() {
		if (!user.getSessionAttributes().containsKey("shib-session-id")) {
			this.user.addSessionAttribute("shib-session-id", "_dd68cbd66641c9b647b05509ac0241fa");
		}
		if (!user.getSessionAttributes().containsKey("shib-session-expires")) {
			this.user.addSessionAttribute("shib-session-expires", "1592847906");
		}
		if (!user.getSessionAttributes().containsKey("shib-identity-provider")) {
			this.user.addSessionAttribute("shib-identity-provider",
					"https://shibboleth.mzk.cz/simplesaml/metadata.xml");
		}
		if (!user.getSessionAttributes().containsKey("shib-authentication-method")) {
			this.user.addSessionAttribute("shib-authentication-method",
					"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
		}

		if (!user.getSessionAttributes().containsKey("shib-handler")) {
			this.user.addSessionAttribute("shib-handler", "https://dnnt.mzk.cz/Shibboleth.sso");
		}
		if (!user.getSessionAttributes().containsKey("remote_user")) {
			this.user.addSessionAttribute("remote_user", "all_users@mzk.cz");
		}
		if (!user.getSessionAttributes().containsKey("affiliation")) {
			this.user.addSessionAttribute("affiliation", "member@mzk.cz");
		}
		if (!user.getSessionAttributes().containsKey("entitlement")) {
			this.user.addSessionAttribute("entitlement", "cokoliv");
		}
		if (!user.getSessionAttributes().containsKey("edupersonuniqueid")) {
			this.user.addSessionAttribute("edupersonuniqueid", "user@mzk.cz");
		}
	}

	protected WebResource.Builder buidFowrardResponse(String url) {
		String prefixHeaders = KConfiguration.getInstance().getConfiguration().getString("cdk.shibboleth.forward.headers");
		
		// no user session attributes in case of no federation
		String header = "";

        boolean shibbolethAttributes = KConfiguration.getInstance().getConfiguration()
                .getBoolean("cdk.collections.sources." + this.source + ".shibboleth_attributes", true);

        
		if (shibbolethAttributes) {
	        Map<String, String> attributes = this.user.getSessionAttributes();
	        header = header + attributes.keySet().stream().map(key -> {
	            return "header_" + key + "=" + attributes.get(key);
	        }).collect(Collectors.joining("|"));
		}

		if (this.remoteAddr != null) {
			header = header + "|" + "header_ip_address=" + this.remoteAddr;
		}
		//TODO: Source 
		if (StringUtils.isAnyString(prefixHeaders)) {
			header = prefixHeaders+header;
		}
		
		LOGGER.fine(String.format("Requesting %s", url));
		WebResource r = client.resource(url);
		LOGGER.info("CDK_TOKEN_PARAMETERS = "+header);
		return r.header("CDK_TOKEN_PARAMETERS", header);
	}

	protected String baseUrl() {
		String baseurl = KConfiguration.getInstance().getConfiguration()
				.getString("cdk.collections.sources." + this.source + ".baseurl");
		return baseurl;
	}
}
