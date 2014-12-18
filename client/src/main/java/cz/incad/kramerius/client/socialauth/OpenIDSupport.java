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
package cz.incad.kramerius.client.socialauth;


import java.io.InputStream;
import java.sql.Savepoint;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.util.SocialAuthUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.client.AuthenticationServlet;
import cz.incad.kramerius.client.kapi.auth.AdminUser;
import cz.incad.kramerius.client.kapi.auth.CallUserController;
import cz.incad.kramerius.client.tools.BasicAuthenticationFilter;
import cz.incad.kramerius.client.tools.GeneratePasswordUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.utils.StringUtils;

public class OpenIDSupport {

    public static final Map<String, String> CREDENTIALS = new HashMap<String, String>();

    public static Logger LOGGER = Logger.getLogger(OpenIDSupport.class
            .getName());

    public Profile getProfile(HttpSession session, HttpServletRequest request,
            HttpServletResponse resp) throws Exception {
        // get the social auth manager from session
        SocialAuthManager manager = (SocialAuthManager) session
                .getAttribute("authManager");

        // call connect method of manager which returns the provider object.
        // Pass request parameter map while calling connect method.
        AuthProvider provider = manager.connect(SocialAuthUtil
                .getRequestParametersMap(request));

        // get profile
        Profile p = provider.getUserProfile();
        return p;
    }

    public void providerLogin(HttpSession session, HttpServletRequest req,
            HttpServletResponse resp) throws Exception {
        String provider = req.getParameter("provider");

        // Create an instance of SocialAuthConfgi object
        SocialAuthConfig config = SocialAuthConfig.getDefault();

        Configuration confObject = KConfiguration.getInstance().getConfiguration();
        Iterator<String> keys = confObject.getKeys();
        Properties props = new Properties();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith("oauth.")) {
                String value = confObject.getString(key);
                String reducedKey = StringUtils.minus(key, "oauth.");
                props.put(reducedKey, value);
            }
        }

        config.load(props);

        // Create an instance of SocialAuthManager and set config
        SocialAuthManager manager = new SocialAuthManager();
        manager.setSocialAuthConfig(config);

        String succUrl = confObject.getString("oauth.successurl");
        if (succUrl == null) {
            String appUrl = ApplicationURL.applicationURL(req);
            if (!appUrl.endsWith("/")) {
                appUrl = appUrl + "/";
            }
            appUrl +="authentication?action=socialLoginRedirect";
        }
        
        
        String redirectingUrl = manager.getAuthenticationUrl(provider, succUrl);

        session.setAttribute("authManager", manager);
        resp.sendRedirect(redirectingUrl);
    }

    public void login(HttpServletRequest request, HttpServletResponse resp) {
        try {
            HttpSession session = request.getSession(true);
            providerLogin(session, request, resp);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    private static String calculateUserName(Profile p) {
        return p.getProviderId() + "_" + p.getValidatedId();
    }

    public static void deleteUser(HttpServletRequest req, String userId) {
        try {
            String url = KConfiguration.getInstance().getConfiguration()
                    .getString("api.point") + "/admin/users/" + userId;

            Client c = Client.create();

            CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req
                    .getSession(true).getAttribute(CallUserController.KEY);
            AdminUser adminCaller = callUserController.getAdminCaller();

            WebResource r = c.resource(url);

            r.addFilter(new BasicAuthenticationFilter(
                    adminCaller.getUserName(), adminCaller.getPassword()));

            String t = r.accept(MediaType.APPLICATION_JSON)
                    .type(MediaType.APPLICATION_JSON).delete(String.class);

        } catch (ClientHandlerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (UniformInterfaceException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static void newPasswordUser(HttpServletRequest req, String userId,
            String pswd) {
        try {
            Client c = Client.create();
            String url = KConfiguration.getInstance().getConfiguration()
                    .getString("api.point")
                    + "/admin/users/"
                    + userId
                    + "/password";

            CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req
                    .getSession(true).getAttribute(CallUserController.KEY);
            AdminUser adminCaller = callUserController.getAdminCaller();

            WebResource r = c.resource(url);
            r.addFilter(new BasicAuthenticationFilter(
                    adminCaller.getUserName(), adminCaller.getPassword()));
            JSONObject object = new JSONObject();
            object.put("password", pswd);

            String t = r.accept(MediaType.APPLICATION_JSON)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(object.toString(), MediaType.APPLICATION_JSON)
                    .put(String.class);
        } catch (UniformInterfaceException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (ClientHandlerException  e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    // create user
    public static String createUser(HttpServletRequest req, Profile profile,
            String password) throws JSONException, ConfigurationException {
        String url = KConfiguration.getInstance().getConfiguration()
                .getString("api.point") + "/admin/users";

        Client c = Client.create();
        WebResource r = c.resource(url);

        CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req
                .getSession(true).getAttribute(CallUserController.KEY);
        AdminUser adminCaller = callUserController.getAdminCaller();

        JSONObject object = new JSONObject();
        object.put("lname", calculateUserName(profile));
        object.put("firstname", profile.getFirstName());
        object.put("surname", profile.getLastName());
        object.put("password", password);

        r.addFilter(new BasicAuthenticationFilter(adminCaller.getUserName(),
                adminCaller.getPassword()));
        String t = r.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(object.toString(), MediaType.APPLICATION_JSON)
                .post(String.class);
        return t;
    }

    private static JSONArray getUser(HttpServletRequest req, Profile p)
            throws ConfigurationException, JSONException {
        Client c = Client.create();
        String url = KConfiguration.getInstance().getConfiguration()
                .getString("api.point")
                + "/admin/users?lname="
                + calculateUserName(p);

        CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req
                .getSession(true).getAttribute(CallUserController.KEY);
        AdminUser adminCaller = callUserController.getAdminCaller();

        WebResource r = c.resource(url);
        r.addFilter(new BasicAuthenticationFilter(adminCaller.getUserName(),
                adminCaller.getPassword()));
        String t = r.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON).get(String.class);
        JSONArray jsonArr = new JSONArray(t);
        return jsonArr;
    }

    public void provideRedirection(HttpServletRequest req,
            HttpServletResponse resp) throws Exception {
        AuthenticationServlet.createCaller(req, null, null, null);
        Profile profile = getProfile(req.getSession(), req, resp);
        JSONArray users = getUser(req, profile);

        String generatedPassword = GeneratePasswordUtils.generatePswd();
        if (users.length() > 0) {
            JSONObject jsonObject = users.getJSONObject(0);
            newPasswordUser(req, "" + jsonObject.getInt("id"),
                    generatedPassword);
        } else {
            createUser(req, profile, generatedPassword);
        }

        users = getUser(req, profile);
        if (users.length() > 0) {
            CallUserController caller = AuthenticationServlet.createCaller(req,
                    calculateUserName(profile), generatedPassword, users
                            .getJSONObject(0).toString());
            caller.getClientCaller().updateInformation(profile.getFirstName(),
                    profile.getLastName());
        }
        resp.sendRedirect("index.vm");
    }

}