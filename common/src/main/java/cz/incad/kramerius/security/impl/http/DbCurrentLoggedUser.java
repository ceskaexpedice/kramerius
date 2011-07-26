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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.impl.http.shibrules.ShibRuleLexer;
import cz.incad.kramerius.security.impl.http.shibrules.ShibRuleParser;
import cz.incad.kramerius.security.impl.http.shibrules.shibs.ShibContext;
import cz.incad.kramerius.security.impl.http.shibrules.shibs.ShibRules;
import cz.incad.kramerius.security.jaas.K4LoginModule;
import cz.incad.kramerius.security.utils.SecurityDBUtils;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.shib.utils.ShibbolethUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

public class DbCurrentLoggedUser extends AbstractLoggedUserProvider {

    public DbCurrentLoggedUser() {
        super();
        LOGGER.fine("Creating db userprovider");
    }

    
    protected void tryToLog(HttpServletRequest httpServletRequest) throws NoSuchAlgorithmException, FileNotFoundException, RecognitionException, TokenStreamException, IOException {
        if (ShibbolethUtils.isUnderShibbolethSession(httpServletRequest)) {
            tryToLogShib(httpServletRequest);
        } else {
            tryToLogDB(httpServletRequest);
        }
    }

    public void tryToLogShib(HttpServletRequest httpServletRequest) throws FileNotFoundException, IOException, RecognitionException, TokenStreamException {
        //String loginName = principal.getName();
        User user = new UserImpl(-1, "", "", "shibuser", 1);
        
        ShibContext ctx = new ShibContext(this.provider.get(), user, this.userManager);
        
        
        String shibRulesPath = KConfiguration.getInstance().getShibAssocRules();
        String readAsString = IOUtils.readAsString(new FileInputStream(shibRulesPath), Charset.forName("UTF-8"), true);
        ShibRuleLexer shibRuleLexer = new ShibRuleLexer(new StringReader(readAsString));
        ShibRuleParser shibRuleParser = new ShibRuleParser(shibRuleLexer);
        
        ShibRules shibRules = shibRuleParser.shibRules();
        shibRules.evaluate(ctx);

        //((UserImpl) user).setGroups((Group[]) groupsList.toArray(new Group[groupsList.size()]));
        cz.incad.kramerius.security.utils.UserUtils.associateCommonGroup(user, userManager);
        HttpServletRequest request = this.provider.get();
        HttpSession session = request.getSession(true);
        if (session.getAttribute(SECURITY_FOR_REPOSITORY_KEY) == null) {
            saveRightsIntoSession(user);
        }
        
        storeLoggedUser(user, session);
    }

    public void tryToLogDB(HttpServletRequest httpServletRequest) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Principal principal = httpServletRequest.getUserPrincipal();
        if (principal != null) {
            String loginName = principal.getName();
            User user = userManager.findUserByLoginName(loginName);
            List<Role> groupsList = new JDBCQueryTemplate<Role>(SecurityDBUtils.getConnection()) {
                @Override
                public boolean handleRow(ResultSet rs, List<Role> retList) throws SQLException {
                    retList.add(SecurityDBUtils.createGroup(rs));
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
            
            storeLoggedUser(user, session);

            
        } else if ((httpServletRequest.getParameter(UserUtils.USER_NAME_PARAM) != null) && (httpServletRequest.getParameter(UserUtils.PSWD_PARAM) != null)) {
            HashMap<String, Object> foundUser = K4LoginModule.findUser(this.connectionProvider.get(), httpServletRequest.getParameter(UserUtils.USER_NAME_PARAM));
            if (foundUser != null) {
                User dbUser = (User) foundUser.get("user");
                String dbPswd = (String) foundUser.get("pswd");
                if (K4LoginModule.checkPswd(httpServletRequest.getParameter(UserUtils.USER_NAME_PARAM), dbPswd, httpServletRequest.getParameter(UserUtils.PSWD_PARAM).toCharArray())) {
                    UserUtils.associateGroups(dbUser, userManager);
                    UserUtils.associateCommonGroup(dbUser, userManager);
                    storeLoggedUser(dbUser, httpServletRequest.getSession(true));
                }
            }
        }
    }


    public void storeLoggedUser(User user, HttpSession session) {
        try {
            session.setAttribute(UserUtils.LOGGED_USER_PARAM, user);
            String key = loggedUsersSingleton.registerLoggedUser(user);
            session.setAttribute(UserUtils.LOGGED_USER_KEY_PARAM, key);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

}
