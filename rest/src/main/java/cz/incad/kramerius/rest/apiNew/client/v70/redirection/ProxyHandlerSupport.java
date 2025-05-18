/*
  * Copyright (C) 2025  Inovatika
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
package cz.incad.kramerius.rest.apiNew.client.v70.redirection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import cz.incad.kramerius.security.Role;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import cz.inovatika.monitoring.ApiCallEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;

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

 /**
  * Base class for implementing various types of ProxyHandlers.
  */
 public abstract class ProxyHandlerSupport {

    public static final boolean DEBUG = false;

    public static final Logger LOGGER = Logger.getLogger(ProxyHandlerSupport.class.getName());

    /** Digital library source */
    protected String source;
    /** Access to solr */
    protected SolrAccess solrAccess;
    /** Requesting user */
    protected User user;
    /** Remote address */
    protected String remoteAddr;
    /**  Instances mangaer */
    protected Instances instances;
    /** Reharvest manager */
    protected ReharvestManager reharvestManager;

    protected CDKRequestCacheSupport cacheSupport;
    protected CloseableHttpClient apacheClient;
    protected DeleteTriggerSupport deleteTriggerSupport;

    public ProxyHandlerSupport(CDKRequestCacheSupport cacheSupport, ReharvestManager reharvestManager, Instances instances, User user,
                               CloseableHttpClient closeableHttpClient,
                               DeleteTriggerSupport triggerSupport,
                               SolrAccess solrAccess, String source, String remoteAddr) {
        this.cacheSupport = cacheSupport;
        this.reharvestManager = reharvestManager;
        this.source = source;
        this.solrAccess = solrAccess;
        this.user = user;
        this.remoteAddr = remoteAddr;
        this.instances = instances;
        this.apacheClient = closeableHttpClient;
        this.deleteTriggerSupport = triggerSupport;
    }

     /**
      * Method for building redirect response
      * @param url Redirecting url
      * @return Redirect response
      * @throws ProxyHandlerException Malformed URL
      */
    public Response buildRedirectResponse(String url) throws ProxyHandlerException {
        try {
            LOGGER.fine(String.format("Redirecting to %s", url));
            return Response.temporaryRedirect(new URL(url).toURI()).build();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new ProxyHandlerException(e);
        }
    }

    public String getSource() {
        return source;
    }


     /**
      * Builds a HEAD forward response using an Apache HTTP client.
      *
      * @param url the requested URL
      * @param mimetype the requested MIME type, or null if not specified
      * @param pid the requested PID
      * @param deleteTrigger whether the handler should execute the delete trigger
      * @param shibHeaders whether to include Shibboleth headers in the request
      * @return a Response object representing the result of the HEAD request
      */
     public Response buildForwardApacheResponseHEAD(String url, String mimetype, String pid, boolean deleteTrigger, boolean shibHeaders) {
        HttpHead head = apacheHead(url, shibHeaders);
        try (CloseableHttpResponse response = apacheClient.execute(head)) {
            int code = response.getCode();
            return Response.status(code).build();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

     /**
      * Builds a GET forward response using an Apache HTTP client.
      *
      * @param url the requested URL
      * @param mimetype the requested MIME type, or null if not specified
      * @param pid the requested PID
      * @param deleteTrigger whether the handler should execute the delete trigger
      * @param shibHeaders whether to include Shibboleth headers in the request
      * @return a Response object representing the result of the HEAD request
      */
     public Response buildForwardApacheResponseGET(String url, String mimetype, String pid, boolean deleteTrigger, boolean shibHeaders, ApiCallEvent event, BiConsumer<byte[], String> dataConsumer) {
        List<Triple<String, Long, Long>> granularTimeSnapshots = event != null ?  event.getGranularTimeSnapshots() : null;
        long start = System.currentTimeMillis();

        HttpGet get = apacheGet(url, shibHeaders);
        String headers = "("+Arrays.stream(get.getHeaders()).map(h-> {
            return String.format("%s = %s", h.getName(), h.getValue());
        }).collect(Collectors.joining(", "))+")";
        LOGGER.log(Level.FINE,  String.format("GET %s %s", url, headers));
        try (CloseableHttpResponse response = apacheClient.execute(get)) {
            int code = response.getCode();
            if (code == 200) {
                long stop = System.currentTimeMillis();

                if (granularTimeSnapshots != null) {
                    granularTimeSnapshots.add(Triple.of(String.format("http/%s", this.getSource()), start,stop));
                }

                LOGGER.log(Level.FINE, String.format(" -> code %d", code));
                HttpEntity entity = response.getEntity();
                long length = entity.getContentLength();
                String responseMimeType = entity.getContentType();

                //TODO: Jak kopirovat data
                byte[] bytes = IOUtils.toByteArray(entity.getContent());
                if (dataConsumer != null) {
                    dataConsumer.accept(bytes, responseMimeType);
                }

                StreamingOutput stream = new StreamingOutput() {
                    public void write(OutputStream output) throws IOException, WebApplicationException {
                        try {
                            IOUtils.copy(new ByteArrayInputStream(bytes), output);
                        } catch (Exception e) {
                            throw new WebApplicationException(e);
                        } finally {
                            EntityUtils.consumeQuietly(entity);
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
                long contentLength = entity.getContentLength();
                if (contentLength >= 0) {
                    respEntity.header("Content-Length", String.valueOf(contentLength));
                }
                return respEntity.build();
            } else {
                // event for reharvest
                if (code == 404) {
                    if (deleteTrigger && this.deleteTriggerSupport != null) {
                        this.deleteTriggerSupport.executeDeleteTrigger(pid);
                    }
                }
                return Response.status(code).build();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    //protected
    protected boolean exists(String url) {
        HttpGet head = apacheGet(url, false);
        try (CloseableHttpResponse response = apacheClient.execute(head)) {
            int code = response.getCode();
            return code == 200;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return false;
    }
    protected InputStream inputStream(String url) {
        HttpGet head = apacheGet(url, false);
        try (CloseableHttpResponse response = apacheClient.execute(head)) {
            int code = response.getCode();
            if (code == 200) {
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                return new ByteArrayInputStream(IOUtils.toByteArray(is));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        return null;
    }



    protected HttpHead apacheHead(String url, boolean headers) {
        LOGGER.fine(String.format("Requesting %s", url));
        HttpHead head = new HttpHead(url);
        head.setHeader("User-Agent", "CDK/1.0");
        if (headers && isAuthenticated() && isDnntUser()) {
            String header = prepareHeader(headers);
            head.setHeader("CDK_TOKEN_PARAMETERS", header);
        }
        return head;
    }

    protected HttpGet apacheGet(String url, boolean headers) {
        LOGGER.fine(String.format("Requesting %s", url));
        HttpGet get = new HttpGet(url);
        get.setHeader("User-Agent", "CDK/1.0");
        if (headers && isAuthenticated() && isDnntUser()) {
            String header = prepareHeader(headers);
            get.setHeader("CDK_TOKEN_PARAMETERS", header);
        }
        return get;
    }

    //TODO: Fix; cannot be associated only with dnnto user
    protected boolean isDnntUser() {
        List<String> names = Arrays.stream(this.user.getGroups()).map(Role::getName).collect(Collectors.toList());
        LOGGER.log(Level.FINE, String.format("Roles %s", names.toString()));
        //TODO: Fix; use constant
        boolean dnntUsersFlag = names.contains("dnnt_users");
        LOGGER.log(Level.FINE, String.format("DNNT user roles %s", ""+dnntUsersFlag));
        return dnntUsersFlag;
    }

    protected boolean isAuthenticated() {
        boolean retval = this.user.getId() != -1;
        LOGGER.log(Level.FINE, String.format("Authenticated user %s", ""+retval));
        return retval;
    }

    protected String userCacheIdentification() {
        if (isAuthenticated()) {
            // eduPersonUniqueId
            // preffered_user_name
            Map<String, String> sessionAttributes = this.user.getSessionAttributes();
            if (sessionAttributes.containsKey("eduPersonUniqueId")) {
                String eduPersonUniquId = sessionAttributes.get("eduPersonUniqueId");
                return eduPersonUniquId;
            }
            if (sessionAttributes.containsKey("preffered_user_name")) {
                String prefferedUserName = sessionAttributes.get("preffered_user_name");
                return prefferedUserName;
            }
            return prepareHeader(true);
        } else {
            return CDKRequestItem.COMMON_USER;
        }
    }

    @NotNull
    protected String prepareHeader(boolean headers) {
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

        LOGGER.log(Level.FINE,"HEADER "+header);
        return header;
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
            this.user.addSessionAttribute("remote_user", "all_users@test.cz");
        }
        if (!user.getSessionAttributes().containsKey("affiliation")) {
            this.user.addSessionAttribute("affiliation", "member@test.cz");
        }
        if (!user.getSessionAttributes().containsKey("entitlement")) {
            this.user.addSessionAttribute("entitlement", "cokoliv");
        }
        if (!user.getSessionAttributes().containsKey("edupersonuniqueid")) {
            this.user.addSessionAttribute("edupersonuniqueid", "user@test.cz");
        }
    }

    protected String baseUrl() {
        String baseurl = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + this.source + ".baseurl");
        return baseurl;
    }
}
