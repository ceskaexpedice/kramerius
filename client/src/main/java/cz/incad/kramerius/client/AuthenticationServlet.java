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


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import biz.sourcecode.base64Coder.Base64Coder;
import cz.incad.kramerius.auth.thirdparty.social.SocialAuthFilter;
import cz.incad.kramerius.client.kapi.auth.AdminUser;
import cz.incad.kramerius.client.kapi.auth.CallUserController;
import cz.incad.kramerius.client.kapi.auth.ClientUser;
import cz.incad.kramerius.client.kapi.auth.ProfileDelegator;
import cz.incad.kramerius.client.kapi.auth.User.UserProvider;
import cz.incad.kramerius.client.kapi.auth.impl.CallUserControllerImpl;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;


/**
 * Authentication servlet  Login and profile functionality
 * @author pavels
 */
public class AuthenticationServlet extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(AuthenticationServlet.class.getName());

    public static String get(String url,String userName, String pswd) throws JSONException {
        Client c = Client.create();
        WebResource r = c.resource(url);
        if (userName != null && pswd != null) {
            r.addFilter(new BasicAuthenticationFilter(userName,pswd));
        }
        Builder builder = r.accept(MediaType.APPLICATION_JSON);
        return builder.get(String.class);
    }
    
    public static String post(String url, JSONObject profile,String userName, String pswd) {
        Client c = Client.create();
        WebResource r = c.resource(url);
        r.addFilter(new BasicAuthenticationFilter(userName, pswd));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(profile.toString()).post(String.class);
        return t;
    }


    public static CallUserController createCaller(HttpServletRequest req, String username,
            String password, String returned) throws JSONException,
            ConfigurationException {
        CallUserController contr = (CallUserController) req.getSession(true).getAttribute(CallUserController.KEY);
        if (contr == null) {
            contr = new CallUserControllerImpl();
            req.getSession().setAttribute(CallUserController.KEY, contr);
        }
        if (returned != null) {
            ((CallUserControllerImpl)contr).setUserJSONRepresentation(new JSONObject(returned));
        }
        if (username != null && password != null) {
            contr.createCaller(username, password, ClientUser.class);
            contr.createCaller(username, password, ProfileDelegator.class);
        }
        if (contr.getAdminCaller() == null) {
            String admUserName = KConfiguration.getInstance().getConfiguration().getString("k4.admin.user");
            String admPswd = KConfiguration.getInstance().getConfiguration().getString("k4.admin.pswd");
            contr.createCaller(admUserName, admPswd, AdminUser.class);
        }
        return contr;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        PostActions aAction = PostActions.valueOf(action);
        try {
            String authUrl = KConfiguration.getInstance().getConfiguration().getString("api.point")+"/user";
            aAction.perform(authUrl, req, resp);
        } catch (JSONException  e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        AuthenticationActions aAction = AuthenticationActions.valueOf(action);
        try {
            String authUrl = KConfiguration.getInstance().getConfiguration().getString("api.point")+"/user";
            aAction.perform(authUrl, req, resp);
        } catch (Exception  e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public static enum PostActions {

        create {

            @Override
            public void perform(String remoteAddr, HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException, IOException,
                    JSONException {

                String encodedProfile = req.getParameter("encodedData");
                if (encodedProfile != null) {
                    byte[] decoded = Base64Coder.decode(encodedProfile);
                    JSONObject jsonObject = new JSONObject(new String(decoded, "UTF-8"));
                    
                    CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req.getSession(true).getAttribute(CallUserController.KEY);
                    if (callUserController != null) {
                        ProfileDelegator profileDelegator = callUserController.getProfileDelegator();
                        String njsonRepre = post(remoteAddr+"/profile", jsonObject, profileDelegator.getUserName(), profileDelegator.getPassword());
                        resp.setContentType("application/json");
                        resp.getWriter().write(jsonObject.toString());
                        //((CallUserControllerImpl)callUserController).setProfileJSONReprestation(new JSONObject(returned));
                        
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    }
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
                }
            }
        },

        savepass {

            @Override
            public void perform(String remoteAddr, HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException, IOException, JSONException {
                String npass = req.getParameter("pswd");
                String oldpass = req.getParameter("opswd");
                
                CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req.getSession(true).getAttribute(CallUserController.KEY);
                if (callUserController != null) {
                    ClientUser clientCaller = callUserController.getClientCaller();
                    // only k5 client
                    String callerPassword = clientCaller.getPassword();
                    if (callerPassword.equals(oldpass)) {
                        UserProvider userProvider = clientCaller.getUserProvider();
                        if (userProvider.equals(UserProvider.K5)) {
                            // 
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("pswd", npass);
                            String nprof = post(remoteAddr, jsonObject, clientCaller.getUserName(), clientCaller.getPassword());
                            if (nprof != null) {
                                synchronized(this) {
                                    clientCaller.updatePassword(npass);
                                    CallUserController.clearCredentials(clientCaller.getUserName());
                                    CallUserController.credentialsTable(clientCaller.getUserName(),npass);
                                }
                            }
                            resp.getWriter().write(nprof);
                            resp.setContentType("application/json");
                            resp.setStatus(HttpServletResponse.SC_OK);
                        } else {
                            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        }
                    } else {
                        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            }
            
        },

        
        profile {
            @Override
            public void perform(String remoteAddr, HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException, IOException, JSONException {
                String encodedProfile = req.getParameter("encodedData");
                if (encodedProfile != null) {
                    byte[] decoded = Base64Coder.decode(encodedProfile);
                    JSONObject jsonObject = new JSONObject(new String(decoded, "UTF-8"));
                    CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req.getSession(true).getAttribute(CallUserController.KEY);
                    if (callUserController != null) {
                        ProfileDelegator profileDelegator = callUserController.getProfileDelegator();
                        String nprof = post(remoteAddr+"/profile", jsonObject, profileDelegator.getUserName(), profileDelegator.getPassword());
                        //((CallUserControllerImpl)callUserController).setProfileJSONReprestation(new JSONObject(nprof));
                        ((CallUserControllerImpl)callUserController).setProfileJSONReprestation(null);
                        
                        resp.setContentType("application/json");
                        resp.getWriter().write(jsonObject.toString());
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    }
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
                }
            }
        };
        public abstract void perform(String remoteAddr, HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException, IOException, JSONException;
    }

        
    public static enum AuthenticationActions {
        
//        socialLoginRedirect {
//
//            @Override
//            public void perform(String remoteAddr, HttpServletRequest req,HttpServletResponse resp) throws UnsupportedEncodingException, IOException, JSONException {
//                try {
//                    OpenIDSupport oidSupport = new OpenIDSupport();
//                    oidSupport.provideRedirection(req,resp);
//                } catch (Exception e) {
//                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
//                }
//            }
//        },
//        
        socialLogin {

            @Override
            public void perform(String remoteAddr, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                String provider = req.getParameter("provider");
                String applicationCotext = ApplicationURL.applicationURL(req);
                String redirectUrl =  applicationCotext + "/index.vm";
                SocialAuthFilter.loginReqests(req, resp, provider, redirectUrl);
            }
        },
        
        login {

            public boolean login(String username, String password, String returned)
                    throws JSONException {
                JSONObject jsonObject = new JSONObject(returned);
                String lname = jsonObject.getString("lname");
                boolean logged = lname.equals(username);
                return logged;
            }

            @Override
            public void perform(String remoteAddr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
                String header = req.getHeader("Authorization");
                if (header != null) {
                    String uname = header.trim().substring("Basic".length()).trim();
                    byte[] decoded = Base64.decodeBase64(uname);
                    String fname = new String(decoded, "UTF-8");
                    if (fname != null && fname.contains(":")) {
                        String username = fname.substring(0, fname.indexOf(':'));
                        String password = fname.substring(fname.indexOf(':')+1);
                        try {
                            String returned = get(remoteAddr, username, password);
                            boolean logged = login(username, password, returned);
                            if (logged) {
                                CallUserController caller = createCaller(req, username, password, returned);
                                JSONObject jsonObject = new JSONObject(returned);
                                String firstname = jsonObject.getString("firstname");
                                String surname  = jsonObject.getString("surname");
                                caller.getClientCaller().updateInformation(firstname, surname);
                            }
                            resp.setContentType("application/json");
                            resp.getWriter().write(returned);
                        } catch (JSONException  e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(),e);
                        } catch (ConfigurationException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(),e);
                        }
                    } 
                } else {
                    resp.setContentType("application/json");
                    resp.getWriter().write("{ \"firstname\": \"not_logged\",\"surname\": \"not_logged\",\"lname\": \"not_logged\",\"roles\": [ {\"name\": \"common_users\"}]}");
                }
            }
        }, 
        logout {
            @Override
            public void perform(String remoteAddr, HttpServletRequest req, HttpServletResponse resp) throws JSONException, IOException {
                req.getSession().invalidate();
                String returned = get(remoteAddr, null, null);
                resp.setContentType("application/json");
                resp.getWriter().write(returned);
            }
        },
        
        
        profile {
            @Override
            public void perform(String remoteAddr, HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException, IOException, JSONException {
                CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req.getSession(true).getAttribute(CallUserController.KEY);
                if (callUserController != null) {
                    if (callUserController.getProfileJSONRepresentation() == null) {
                        ProfileDelegator profileDelegator = callUserController.getProfileDelegator();
                        String returned = get(remoteAddr+"/profile", profileDelegator.getUserName(), profileDelegator.getPassword());
                        ((CallUserControllerImpl)callUserController).setProfileJSONReprestation(new JSONObject(returned));
                    }
                    resp.setContentType("application/json");
                    resp.getWriter().write(callUserController.getProfileJSONRepresentation().toString());
                } else {
                    resp.setContentType("application/json");
                    resp.getWriter().write("{}");
                }
            }
            
        };
        public abstract void perform(String remoteAddr, HttpServletRequest req, HttpServletResponse resp) throws Exception;
    }
}
