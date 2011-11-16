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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.GeneratePDFServlet.Action;
import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.pdf.GeneratePDFService;
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
    
    

    public enum PostActions {
        POST{
            @Override
            public void process(HttpServletRequest request, HttpServletResponse response, User user, UserProfileManager profileManager) {

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
                    response.setContentType("text/plain");
                    response.getWriter().println(profile.getRawData());
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }
            
        };
        public abstract void process(HttpServletRequest request, HttpServletResponse response, User user, UserProfileManager profileManager);
    }

}
