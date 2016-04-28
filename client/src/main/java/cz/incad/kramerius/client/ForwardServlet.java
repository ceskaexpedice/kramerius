/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.client;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;

import cz.incad.kramerius.client.forward.DefaultModify;
import cz.incad.kramerius.client.forward.URLPathModify;
import cz.incad.kramerius.client.kapi.auth.CallUserController;
import cz.incad.kramerius.client.kapi.auth.User;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;
import cz.incad.utils.IOUtils;
import cz.incad.utils.StringUtils;

public class ForwardServlet extends HttpServlet {

    public static final Map<String, String> JSON_RESULTS_CHACHE = new HashMap<String,String>();

    private static final String URL_PREFIX_KEY = "prefix";
    private static final String URL_PATH_MODIF_KEY = "urlmodif";

    private static final String READ_TIMEOUT_KEY = "readTimeout";
    private static final String CON_TIMEOUT_KEY = "conTimeout";

    public static final Logger LOGGER = Logger.getLogger(ForwardServlet.class.getName());
    
    private Map<String, URLPathModify> MODIFIERS = new HashMap<String, URLPathModify>();
    
    public static enum TypeOfCall {

        ADMIN {
            @Override
            public User getUser(CallUserController cus) {
                if (cus != null) {
                    return cus.getAdminCaller();
                } else {
                    return null;
                }
            }
        },
        USER {
            @Override
            public User getUser(CallUserController cus) {
                if (cus != null) {
                    return cus.getClientCaller();
                } else {
                    return null;
                }
            }
        };

        public abstract User getUser(CallUserController cus);
    }

    
    public URLPathModify getPathModifier(String clzName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (!MODIFIERS.containsKey(clzName)) {
            Class<URLPathModify> clz = (Class<URLPathModify>) Class.forName(clzName);
            MODIFIERS.put(clzName, clz.newInstance());
        }
        return MODIFIERS.get(clzName);
    }
    
    
    public static ClientResponse method(String url, SupportedMethods method,
            JSONObject jsonObject, String userName, String pswd, String acceptMimeType, String contentType)
            throws JSONException {
        Client c = Client.create();

        c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);

        WebResource r = c.resource(url);
        if (userName != null && pswd != null) {
            r.addFilter(new BasicAuthenticationFilter(userName,
                    pswd));
        }
        Builder builder = r.accept(acceptMimeType);

        if (jsonObject != null) {
            builder = builder.type(contentType).entity(
                    jsonObject.toString(), contentType);
        }

        switch (method) {
            case GET: 
                return builder.get(ClientResponse.class);
            case PUT:
                return builder.put(ClientResponse.class);
            case DELETE:
                return builder.delete(ClientResponse.class);
            case POST:
                return builder.post(ClientResponse.class);
            default:
                throw new IllegalStateException("usupported type of method");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            TypeOfCall tc = disectTypeOfCall(req);
            User user = tc.getUser((CallUserController) req.getSession()
                    .getAttribute(CallUserController.KEY));

            String urlPrefixKey = getInitParameter(URL_PREFIX_KEY);
            String prefixAddr = KConfiguration.getInstance().getConfiguration().getString(urlPrefixKey);
            if(prefixAddr == null || StringUtils.isEmptyString(prefixAddr)) throw new RuntimeException("expecting property "+urlPrefixKey);
            
            String queryString = req.getQueryString();
            String requestedURI = req.getRequestURI();
            
            String urlModif = getInitParameter(URL_PATH_MODIF_KEY);
            if (urlModif != null) {
                URLPathModify pathModifier = getPathModifier(urlModif);
                requestedURI =  pathModifier.modifyPath(requestedURI, req);
                queryString = pathModifier.modifyQuery(queryString, req);
            } else {
                URLPathModify pathModifier = getPathModifier(DefaultModify.class.getName());
                requestedURI =  pathModifier.modifyPath(requestedURI, req);
                queryString = pathModifier.modifyQuery(queryString, req);
            }

            String replaceURL = prefixAddr + requestedURI;
            if (StringUtils.isAnyString(queryString)) {
                replaceURL = replaceURL + "?" + queryString;
            }
            LOGGER.info("requesting url " + replaceURL);

            JSONObject jsonObj = null;
            if (req.getContentLength() > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ServletInputStream iStream = req.getInputStream();
                IOUtils.copyStreams(iStream, bos);

                String t = new String(bos.toByteArray(),
                        Charset.forName("UTF-8"));

                if ((!t.startsWith("{")) && (!t.endsWith("}"))) {
                    // hack because of jquery
                    jsonObj = new JSONObject("{" + t + "}");
                } else {
                    jsonObj = new JSONObject(t);
                }
            }

            ClientResponse clientResponse = null;
            if (user != null) {
                clientResponse = method(replaceURL, SupportedMethods.POST, jsonObj,
                        user.getUserName(), user.getPassword(), req.getHeader("Accept"),req.getHeader("Content-Type"));
            } else {
                clientResponse = method(replaceURL, SupportedMethods.POST, jsonObj, null,
                        null, req.getHeader("Accept"),req.getHeader("Content-Type"));
            }
            resp.setContentType(clientResponse.getType().toString());
            resp.getWriter().write(clientResponse.getEntity(String.class));
        } catch (JSONException  e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch ( InstantiationException  e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            TypeOfCall tc = disectTypeOfCall(req);
            User user = tc.getUser((CallUserController) req.getSession()
                    .getAttribute(CallUserController.KEY));

            String prefixKey = getInitParameter(URL_PREFIX_KEY);
            String prefixAddr = KConfiguration.getInstance().getConfiguration().getString(prefixKey);
            if(prefixAddr == null || StringUtils.isEmptyString(prefixAddr)) throw new RuntimeException("expecting property "+prefixKey);

            String queryString = req.getQueryString();
            String requestedURI = req.getRequestURI();

            String urlModif = getInitParameter(URL_PATH_MODIF_KEY);
            if (urlModif != null) {
                URLPathModify pathModifier = getPathModifier(urlModif);
                requestedURI =  pathModifier.modifyPath(requestedURI, req);
                queryString = pathModifier.modifyQuery(queryString, req);
            } else {
                URLPathModify pathModifier = getPathModifier(DefaultModify.class.getName());
                requestedURI =  pathModifier.modifyPath(requestedURI, req);
                queryString = pathModifier.modifyQuery(queryString, req);
            }

            String replaceURL = prefixAddr + requestedURI;
            if (StringUtils.isAnyString(queryString)) {
                replaceURL = replaceURL + "?" + queryString;
            }
            
            // settings
            String readTimeOut = getInitParameter(READ_TIMEOUT_KEY);
            String conTimeOut = getInitParameter(CON_TIMEOUT_KEY);
            Map<String, String> settings = new HashMap<String, String>();
            if (readTimeOut != null) {
                settings.put(RESTHelper.READ_TIMEOUT, readTimeOut);
            }

            if (conTimeOut != null) {
                settings.put(RESTHelper.CONNECTION_TIMEOUT, conTimeOut);
            }
            
            
            InputStream inputStream = null;
            if (user != null) {

                fileDisposition(req, resp);
                RESTHelper.fillResponse(replaceURL, user.getUserName(),
                        user.getPassword(), req,resp, settings);
            } else {
                fileDisposition(req, resp);
                RESTHelper.fillResponse(replaceURL, null,
                        null, req, resp, settings);
            }
        } catch (InstantiationException  e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (ClassNotFoundException  e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }


    private void fileDisposition(HttpServletRequest req,
            HttpServletResponse resp) {
        String asFileParam = req.getParameter("asFile");
        if ((asFileParam != null) && (asFileParam.equals("true"))) {
            String fname = "original";
            if (req.getRequestURI().indexOf("uuid:")> 0) {
                String[] parts= req.getRequestURI().split("/");
                for (String part : parts) {
                    if (part.startsWith("uuid:")) {
                        fname=fname+"_"+part;
                    }
                }
                resp.setHeader("Content-disposition", "attachment; filename=" + fname);
            }
        }
    }

    private TypeOfCall disectTypeOfCall(HttpServletRequest req) {
        String requestedURI = req.getRequestURI();
        return requestedURI.contains("admin") ? TypeOfCall.ADMIN
                : TypeOfCall.USER;
    }

    private static enum SupportedMethods {
        PUT, GET, POST, DELETE;
    }
    
}
