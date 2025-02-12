package cz.incad.kramerius.rest.apiNew.client.v70.redirection;

import static cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem.LIBRARIES_KEYWORD;
import static cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem.OWN_PID_PATH;
import static cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem.ROOT_PID;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.AlreadyRegistedPidsException;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem.TypeOfReharvset;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.utils.IntrospectUtils;
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

    public ProxyHandlerSupport(ReharvestManager reharvestManager, Instances instances, User user, Client client,
            SolrAccess solrAccess, String source, String remoteAddr) {
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

    public String getSource() {
        return source;
    }


    /**
     * Build rewsponse with HEAD method
     * 
     * @param url
     * @return
     * @throws ProxyHandlerException
     */
    public Response buildForwardResponseHEAD(String url) throws ProxyHandlerException {
        WebResource.Builder b = buidForwardResponse(url);
        ClientResponse clientResponseHead = b.head();
        if (clientResponseHead.getStatus() == 200) {
            return Response.status(200).build();
        } else {
            return Response.status(clientResponseHead.getStatus()).build();
        }
    }

    public ClientResponse forwardedResponse(String url) throws ProxyHandlerException {
        WebResource.Builder b = buidForwardResponse(url);
        ClientResponse response = b.get(ClientResponse.class);
        if (response.getStatus() == 200) {
            return response;
        } else {
            throw new ProxyHandlerException("Bad response; status code " + response.getStatus());
        }
    }

    public Response buildForwardResponseGET(String url, boolean deleteTrigger) throws ProxyHandlerException {
        return buildForwardResponseGET(url, null, null, deleteTrigger, true);
    }

    public Response buildForwardResponseGET(String url, String pid, boolean deleteTrigger)
            throws ProxyHandlerException {
        return buildForwardResponseGET(url, null, pid, deleteTrigger, true);
    }

    public Response buildForwardResponseGET(String url, String mimetype, String pid, boolean deleteTrigger, boolean shibHeaders)
            throws ProxyHandlerException {
        // tady se konstruuje klient, predavaji se hlavicky
        WebResource.Builder b = buidForwardResponse(url, shibHeaders);
        ClientResponse response =null;
        try {
            response = b.get(ClientResponse.class);
            LOGGER.info("Status code response "+response.getStatus() + ",Mimetype "+mimetype+", pid "+pid+", deleteTrigger "+deleteTrigger+", shibHeaders "+shibHeaders);
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

                /*
                 * Disable header forward headers.keySet().forEach(key -> { List<String> values
                 * = headers.get(key); values.stream().forEach(val -> { respEntity.header(key,
                 * val); }); });
                 */

                return respEntity.build();
            } else {
                // event for reharvest
                if (response.getStatus() == 404) {
                    if (deleteTrigger)
                        deleteTriggeToReharvest(pid);
                }
                return Response.status(response.getStatus()).build();
            }
        } finally {
            if (response != null) response.close();
        }
    }

    public void deleteTriggeToReharvest(String pid) {
        if (reharvestManager != null && pid != null) {
            try {
                
                String cdkRootPid = null;
                String cdkOwnPidPath = null;
                String cdkOwnParentPid = null;

                
                Document solrDataByPid = this.solrAccess.getSolrDataByPid(pid);
                Element rootPidElm = XMLUtils.findElement(solrDataByPid.getDocumentElement(),
                        new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                if (element.getNodeName().equals("str")) {
                                    String fieldName = element.getAttribute("name");
                                    return fieldName.equals("root.pid");
                                }
                                return false;
                            }
                });
                cdkRootPid = rootPidElm != null ? rootPidElm.getTextContent() : null;
                Element ownPidPath = XMLUtils.findElement(solrDataByPid.getDocumentElement(),
                        new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                if (element.getNodeName().equals("str")) {
                                    String fieldName = element.getAttribute("name");
                                    return fieldName.equals("own_pid_path");
                                }
                                return false;
                            }
                });
                cdkOwnPidPath = ownPidPath != null ? ownPidPath.getTextContent() : null;
                
                Element ownParentPidElm = XMLUtils.findElement(solrDataByPid.getDocumentElement(),
                        new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                if (element.getNodeName().equals("str")) {
                                    String fieldName = element.getAttribute("name");
                                    return fieldName.equals("own_parent.pid");
                                }
                                return false;
                            }
                });
                cdkOwnParentPid = ownParentPidElm != null ? ownParentPidElm.getTextContent() : null;

                if (rootPidElm != null && ownPidPath != null && ownParentPidElm != null) {
                    String pidPath = ownPidPath.getTextContent().trim();
                    String ownParentPidText = ownParentPidElm.getTextContent().trim();
                    int index = pidPath.indexOf(ownParentPidText);
                    if (index >= 0) {
                        pidPath = pidPath.substring(0, index + ownParentPidText.length()).trim();
                    }
                    
                    try {

                        Map<String, JSONObject> map = new HashMap<>();
                        
                        JSONObject jsonResult = IntrospectUtils.introspectSolr(this.client, this.instances, ownParentPidText,false);
                        Set keys = jsonResult.keySet();
                        for (Object keyObj : keys) {
                            String key = keyObj.toString();
                            JSONObject solrResult = jsonResult.getJSONObject(key);
                            JSONObject response = solrResult.getJSONObject("response");
                            int numFound = response.optInt("numFound");
                            if (numFound > 0) {
                                JSONObject doc =  response.getJSONArray("docs").getJSONObject(0);
                                map.put(key, doc);
                                
                                /*
                                 *  
                                 *  {
                                      "fedora.model": "periodical",
                                      "root.pid": "uuid:1c869c00-535b-11e3-9ea2-5ef3fc9ae867",
                                      "pid_paths": [
                                        "uuid:1c869c00-535b-11e3-9ea2-5ef3fc9ae867"
                                      ],
                                      "PID": "uuid:1c869c00-535b-11e3-9ea2-5ef3fc9ae867",
                                      "pid": "uuid:1c869c00-535b-11e3-9ea2-5ef3fc9ae867",
                                      "model": "periodical",
                                      "pid_path": [
                                        "uuid:1c869c00-535b-11e3-9ea2-5ef3fc9ae867"
                                      ],
                                      "root_pid": "uuid:1c869c00-535b-11e3-9ea2-5ef3fc9ae867"
                                 *  }
                                 *   
                                 */
                                
                            }
                        }
                        
                        
                        ReharvestItem alreadyRegistredItem = this.reharvestManager.getOpenItemByPid(ownParentPidText);
                        if (alreadyRegistredItem == null) {
                            Document onwParentPidDocument = this.solrAccess.getSolrDataByPid(ownParentPidElm.getTextContent().trim());
                            Element cdkModelElement = onwParentPidDocument!= null ? XMLUtils.findElement(onwParentPidDocument.getDocumentElement(),
                                new XMLUtils.ElementsFilter() {
                                    @Override
                                    public boolean acceptElement(Element element) {
                                        if (element.getNodeName().equals("str")) {
                                            String fieldName = element.getAttribute("name");
                                            return fieldName.equals("model");
                                        }
                                        return false;
                                    }
                            }) : null;
                            
                            ReharvestItem reharvestItem = new ReharvestItem(UUID.randomUUID().toString(), "Delete trigger|404 ", "open", ownParentPidElm.getTextContent().trim(), pidPath);
                            List<String> topLevelModels = Lists.transform(KConfiguration.getInstance().getConfiguration().getList("fedora.topLevelModels"), Functions.toStringFunction());

                            LinkedHashSet<String> uniqueRootPids = new LinkedHashSet<>();
                            map.keySet().forEach(key-> {
                                String rootPid = map.get(key).optString("root.pid");
                                uniqueRootPids.add(rootPid);
                                
                            });                            
                            
                            LinkedHashSet<String> uniqueModels = new LinkedHashSet<>();
                            map.keySet().forEach(key-> {
                                String model = map.get(key).optString("model");
                                uniqueModels.add(model);
                            });
                            
                            
                            if (uniqueModels.size() == 1) {
                                String model = uniqueModels.iterator().next();
                                if (cdkModelElement != null) {
                                    String cdkModel = cdkModelElement.getTextContent().trim(); 
                                    if (!cdkModel.equals(model)) {
                                        //TODO: cdk conflict 
                                        // delete - followed by reharvest
                                    } 
                                }
                                if (topLevelModels.contains(model)) {
                                    reharvestItem.setTypeOfReharvest(TypeOfReharvset.root);
                                } else {
                                    reharvestItem.setTypeOfReharvest(TypeOfReharvset.children);
                                }
                            } else if (uniqueModels.size() > 1){
                                // live conflict - reharvest dle nkp 
                            }
                            reharvestItem.setLibraries(new ArrayList<>( map.keySet()) );
                            //reharvestItem.setLibraries(pair.getRight());
                            reharvestItem.setState("waiting_for_approve");
                            this.reharvestManager.register(reharvestItem,false);
                        }
                    } catch (DOMException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    } catch (AlreadyRegistedPidsException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                } else {
                    LOGGER.log(Level.SEVERE, "Cannot find root.pid element");
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            }
        } else {
            LOGGER.log(Level.SEVERE, "No reharvest manager or pid ");
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
    protected WebResource.Builder buidForwardResponse(String url) {
    	return buidForwardResponse(url, true);
    }


    
    protected WebResource.Builder buidForwardResponse(String url, boolean headers) {
        String prefixHeaders = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.shibboleth.forward.headers");

        // no user session attributes in case of no federation
        String header = "";

        boolean shibbolethAttributes = KConfiguration.getInstance().getConfiguration()
                .getBoolean("cdk.collections.sources." + this.source + ".shibboleth_attributes", true);

        if (shibbolethAttributes && headers) {
            Map<String, String> attributes = this.user.getSessionAttributes();
            header = header + attributes.keySet().stream().map(key -> {
                return "header_" + key + "=" + attributes.get(key);
            }).collect(Collectors.joining("|"));
        }

        if (this.remoteAddr != null && headers) {
            header = header + "|" + "header_ip_address=" + this.remoteAddr;
        }
        // TODO: Source
        if (StringUtils.isAnyString(prefixHeaders)) {
            header = prefixHeaders + header;
        }

        
        LOGGER.fine(String.format("Requesting %s", url));
        WebResource r = client.resource(url);
        if (headers) {
            String message = String.format("URL(%s), CDK_TOKEN_PARAMETERS(%s)", url, header);
            LOGGER.fine(message);
            return r.header("CDK_TOKEN_PARAMETERS", header);
        } else {
            return r.getRequestBuilder();
        }
    }

    protected String baseUrl() {
        String baseurl = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + this.source + ".baseurl");
        return baseurl;
    }
}
