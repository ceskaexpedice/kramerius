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

import javax.jms.IllegalStateException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.Configuration;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.util.SocialAuthUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.auth.utils.GeneratePasswordUtils;
import cz.incad.kramerius.client.AuthenticationServlet;
import cz.incad.kramerius.client.kapi.auth.CallUserController;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.utils.StringUtils;

import cz.incad.kramerius.auth.UsersWrapper;

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


    public static class OpenIdUserWrapper implements UsersWrapper {

        private Profile p;
        
        public OpenIdUserWrapper(Profile p) {
            super();
            this.p = p;
        }

        @Override
        public String getCalculatedName() {
            return p.getProviderId() + "_" + p.getValidatedId();
        }

        @Override
        public String getProperty(String key) {
            if (key.equals(UserUtils.FIRST_NAME_KEY)) {
                return p.getFirstName();
            } else if (key.equals(UserUtils.LAST_NAME_KEY)) {
                return p.getLastName();
            } else  return null;
        }
    }
    
    public void provideRedirection(HttpServletRequest req,
            HttpServletResponse resp) throws Exception {

        AuthenticationServlet.createCaller(req, null, null, null);
        Profile profile = getProfile(req.getSession(), req, resp);
        OpenIdUserWrapper owrap = new OpenIdUserWrapper(profile);
        JSONArray users = ProviderUsersUtils.getUser(req, owrap);
        
        String generatedPassword = GeneratePasswordUtils.generatePswd();
        if (users.length() > 0) {
            JSONObject jsonObject = users.getJSONObject(0);
            ProviderUsersUtils.newPasswordUser(req, "" + jsonObject.getInt("id"),
                    generatedPassword);
        } else {
            ProviderUsersUtils.createUser(req, owrap, generatedPassword);
        }

        users = ProviderUsersUtils.getUser(req, new OpenIdUserWrapper(profile));
        if (users.length() > 0) {
            CallUserController caller = AuthenticationServlet.createCaller(req,
                    owrap.getCalculatedName(), generatedPassword, users
                            .getJSONObject(0).toString());
            caller.getClientCaller().updateInformation(profile.getFirstName(),
                    profile.getLastName());
            
        }
        resp.sendRedirect("index.vm");
    }

}