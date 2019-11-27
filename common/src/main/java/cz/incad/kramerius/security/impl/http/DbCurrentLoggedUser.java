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
package cz.incad.kramerius.security.impl.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.jaas.K4LoginModule;
import cz.incad.kramerius.security.utils.SecurityDBUtils;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

public class DbCurrentLoggedUser extends AbstractLoggedUserProvider {

    public static final String SHIB_USER_KEY="SHIB_USER_KEY";

    public DbCurrentLoggedUser() {
        super();
        LOGGER.fine("Creating db userprovider");
    }

    public User getPreviousLoggedUser(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession();
        if (session != null) {
            if (this.loggedUsersSingleton.isLoggedUser(this.provider)) {
                return getSessionUser(session);
            } else return null;
        }
        else return null;
    }

    

    public void clearSessionUser(HttpSession session) {
        if (session.getAttribute(UserUtils.LOGGED_USER_PARAM) != null) {
            session.removeAttribute(UserUtils.LOGGED_USER_PARAM);
        }
    }

    public User getSessionUser(HttpSession session) {
        User loggedUser = (User) session.getAttribute(UserUtils.LOGGED_USER_PARAM);
        if (loggedUser != null) {
            return loggedUser;
        } else return null;
    }


    protected void tryToLog(HttpServletRequest httpServletRequest) throws NoSuchAlgorithmException, FileNotFoundException, RecognitionException, TokenStreamException, IOException {
        tryToLogDB(httpServletRequest);
    }


    public void tryToLogDB(HttpServletRequest httpServletRequest) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Principal principal = httpServletRequest.getUserPrincipal();
        if (principal != null) {
            String loginName = principal.getName();
            User user = userManager.findUserByLoginName(loginName);
            if (user != null) {

            	List<Role> groupsList = new JDBCQueryTemplate<Role>(SecurityDBUtils.getConnection()) {
                    @Override
                    public boolean handleRow(ResultSet rs, List<Role> retList) throws SQLException {
                        retList.add(SecurityDBUtils.createRole(rs));
                        return true;
                    }
                }.executeQuery("select * from user_group_mapping where user_id=?", user.getId());

                // TODO:Zmenit
                ((UserImpl) user).setGroups((Role[]) groupsList.toArray(new Role[groupsList.size()]));

                // User user = k4principal.getUser();
                cz.incad.kramerius.security.utils.UserUtils.associateCommonGroup(user, userManager);

                HttpServletRequest request = this.provider.get();
                HttpSession session = request.getSession(true);
                if (session.getAttribute(SECURITY_FOR_REPOSITORY_KEY) == null) {
                    saveRightsIntoSession(user);
                }

                final Locale foundLocale = localeFromProfile(user);

                final Map<String, String> PREPARED_PROFILE = new HashMap<String, String>();
                PREPARED_PROFILE.put("columns", getColumnsFromProfile(user));

                storeLoggedUser(user,  new HashMap<String, Object>(){{
                    put(SHIB_USER_KEY,"false");
                    if (foundLocale != null) {
                        put("client_locale",foundLocale);
                    }
                    put("PREPARING_PROFILE",PREPARED_PROFILE);
                }});
            }
        } else if ((httpServletRequest.getParameter(UserUtils.USER_NAME_PARAM) != null) && (httpServletRequest.getParameter(UserUtils.PSWD_PARAM) != null)) {
            HashMap<String, Object> foundUser = K4LoginModule.findUser(this.connectionProvider.get(), httpServletRequest.getParameter(UserUtils.USER_NAME_PARAM));
            if (foundUser != null) {
                User dbUser = (User) foundUser.get("user");
                String dbPswd = (String) foundUser.get("pswd");
                if (K4LoginModule.checkPswd(httpServletRequest.getParameter(UserUtils.USER_NAME_PARAM), dbPswd, httpServletRequest.getParameter(UserUtils.PSWD_PARAM).toCharArray())) {
                    UserUtils.associateGroups(dbUser, userManager);
                    UserUtils.associateCommonGroup(dbUser, userManager);
                    final Locale foundLocale = localeFromProfile(dbUser);

                    final Map<String, String> PREPARED_PROFILE = new HashMap<String, String>();
                    PREPARED_PROFILE.put("columns", getColumnsFromProfile(dbUser));

                    storeLoggedUser(dbUser,  new HashMap<String, Object>(){{
                        put(SHIB_USER_KEY,"false");
                        if (foundLocale != null) {
                            put("client_locale",foundLocale);
                        }
                        put("PREPARING_PROFILE",PREPARED_PROFILE);
                    }});
                }
            }
        }
    }


    public Locale localeFromProfile(User user) {
        try {
            UserProfile profile = this.userProfileManager.getProfile(user);
            String lang =  "";
            
            if (profile != null && profile.getJSONData().has("client_locale")){
                lang = profile.getJSONData().getString("client_locale");
            } else{
                return null;
            }
            final Locale foundLocale = this.textsService.findLocale(lang);
            return foundLocale;
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public String getColumnsFromProfile(User user) {
        try {
            UserProfile profile = this.userProfileManager.getProfile(user);
            if (profile.getJSONData().has("results")) {
                JSONObject results = profile.getJSONData().getJSONObject("results");
                if (results.has("columns")) {
                    return results.getString("columns");
                }
            }
            return "2";
        } catch (JSONException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    public void storeLoggedUser(User user,  Map<String, Object> additionalValues) {
        try {
            HttpSession session = this.provider.get().getSession();
            session.setAttribute(UserUtils.LOGGED_USER_PARAM, user);
            String key = loggedUsersSingleton.registerLoggedUser(user);
            session.setAttribute(UserUtils.LOGGED_USER_KEY_PARAM, key);
            Set<String> keySet = additionalValues.keySet();
            for (String k : keySet) {
                session.setAttribute(k, additionalValues.get(k));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

}
