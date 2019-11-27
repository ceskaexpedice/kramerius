/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.Kramerius;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import biz.sourcecode.base64Coder.Base64Coder;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.users.ProfilePrepareUtils;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.users.UserProfileManager;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ProfilesServlet extends GuiceServlet {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProfilesServlet.class.getName());
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    KConfiguration configuration;
    
    @Inject
    UserProfileManager userProfileManager;
    
    @Inject
    Provider<User> userProvider;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        GetActions.valueOf(action).process(req, resp, this.userProvider.get(), this.userProfileManager); 
    }
    
    
    

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        PostActions.valueOf(action).process(req, resp, this.userProvider.get(), this.userProfileManager);
    }




    public enum PostActions {
        POST{
            @Override
            public void process(HttpServletRequest request, HttpServletResponse response, User user, UserProfileManager profileManager) {
                String encodedProfile = request.getParameter("encodedData");
                if (encodedProfile != null) {
                    try {
                        byte[] decoded = Base64Coder.decode(encodedProfile);
                        JSONObject jsonNObject = new JSONObject(new String(decoded));

                        UserProfile profile = profileManager.getProfile(user);
                        profile.setJSONData(jsonNObject);
                        profileManager.saveProfile(user, profile);
                    } catch (JSONException e) {
                        throw new IllegalStateException(e.getMessage());
                    }
                }
            }

        };
        
        public abstract void process(HttpServletRequest request, HttpServletResponse response, User user, UserProfileManager profileManager);
    }
    
    public enum GetActions {
        GET {
            @Override
            public void process(HttpServletRequest request, HttpServletResponse response, User user, UserProfileManager profileManager) {
                try {
                    UserProfile profile = profileManager.getProfile(user);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().println(profile.getRawData());
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }
        },
        UPDATE_FIELD {

            @Override
            public void process(HttpServletRequest request, HttpServletResponse response, User user, UserProfileManager profileManager) {
                try {
                    UserProfile profile = profileManager.getProfile(user);
                    JSONObject jsonData = profile.getJSONData();
                    String field = request.getParameter("field");
                    String value = request.getParameter("value");
                    jsonData.put(field, value);
                    profile.setJSONData(jsonData);
                    profileManager.saveProfile(user, profile);
                } catch (JSONException e) {
                    throw new IllegalStateException(e.getMessage());
                }
            }
            
        }, 
        PREPARE_FIELD_TO_SESSION {

            @Override
            public void process(HttpServletRequest request, HttpServletResponse response, User user, UserProfileManager profileManager) {
                HttpSession session = request.getSession(true);

                String[] fpars = request.getParameterValues("field");
                String[] keys = request.getParameterValues("key");
                for (int i = 0; i < keys.length; i++) {
                    String key = keys[i];
                    String fpar = fpars[i];
                    ProfilePrepareUtils.prepareProperty(session, key, fpar);
                    
                }
            }
        };
        
        
        

        public abstract void process(HttpServletRequest request, HttpServletResponse response, User user, UserProfileManager profileManager);
    }

}
