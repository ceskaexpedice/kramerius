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
package cz.incad.Kramerius.views.rights;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumLoader;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;


public class DisplayRightView extends AbstractRightsView {

    public static final String ACTION="action";

    @Inject
    UserManager userManager;
    
    @Inject
    Provider<User> userProvider;

    
    @Inject
    RightCriteriumWrapperFactory factory;

    @Inject
    RightsManager rightsManager;
    
    
    Right right;
    
    
    public String getAppliedRole() throws RecognitionException, TokenStreamException {
        if (getAction() == Actions.create) {
            return "common_users";
        } else {
            Right right = getRight();
            //TODO: change it !!
            Role role = (Role) right.getUser();
            return role.getName();
        }
    }

    public Right getRight() throws RecognitionException, TokenStreamException {
        if ((this.right == null)  && (getRightIdParam() != null)){
            this.right = this.rightsManager.findRightById(Integer.parseInt(getRightIdParam()));
        }
        return this.right;
    }
    
    public String[] getRoles() {
        User user = this.userProvider.get();
        Role[] roles = null;
        
        if (hasSuperAdminRole(this.userProvider.get())) {
            roles = userManager.findAllRoles("");
        } else {
            int[] grps = getUserGroups(user);
            roles = userManager.findAllRoles(grps, "");
        }
        return getRoleNames(roles);
    }

    
    public String[] getRoleNames(Role[] roles) {
        String[] strRoles = new String[roles.length];
        for (int i = 0; i < strRoles.length; i++) {
            String rname = roles[i].getName();
            rname = rname.replace("\"", "\\\"");
            strRoles[i] = rname;
        }
        return strRoles;
    }


    public int[] getUserGroups(User user) {
        Role[] grps = user.getGroups();
        int[] grpIds = new int[grps.length];
        for (int i = 0; i < grpIds.length; i++) {
            grpIds[i] = grps[i].getId();
        }
        return grpIds;
    }

    
    public int getCritparamsid() throws RecognitionException, TokenStreamException {
        RightCriteriumParams params = getParams();
        return params != null ? params.getId() : 0; 
    }

    
    public int getPriority() throws RecognitionException, TokenStreamException {
        Right r = getRight();
        if (r != null ) {
            return r.getFixedPriority();
        } else return 0;
    }
    
    public RightCriteriumParams getParams() throws RecognitionException, TokenStreamException {
        Right r = getRight();
        if (r != null && r.getCriteriumWrapper() != null && r.getCriteriumWrapper().getCriteriumParams() != null) {
            return r.getCriteriumWrapper().getCriteriumParams();
        } else return null;
        
    }
    
    public String getCritparamdesc() throws RecognitionException, TokenStreamException {
        RightCriteriumParams params = getParams();
        return params != null ? params.getShortDescription() : ""; 
    }
    
    public Object[] getCritparams() throws RecognitionException, TokenStreamException {
        RightCriteriumParams params = getParams();
        if (params != null) {
            List<Object> retvals = new ArrayList<>();
            Object[] objs = params.getObjects();
            for (Object o :
                    objs) {
                if (o instanceof String) {
                    String str = (String) o;
                    // rendering to javascript '\' must be escaped
                    // replacing what = \
                    StringBuilder what = new StringBuilder();
                    what.append("\\\\");
                    // replacing by = \\
                    StringBuilder byWhat = new StringBuilder();
                    byWhat.append("\\\\\\\\");

                    String s = str.replaceAll(what.toString(), byWhat.toString());
                    retvals.add(s);
                } else {
                    retvals.add(o);
                }

            }
            return retvals.toArray();

        } else return new Object[0];
    }

    public String getCriterium() throws RecognitionException, TokenStreamException {
        Right r = getRight();
        if (r != null) {
            return r.getCriteriumWrapper() != null ? r.getCriteriumWrapper().getRightCriterium().getQName() : "";
        } else return "";
    }
    
    public List<RightCriteriumWrapper> getCriteriums() {
        List<RightCriteriumWrapper> criteriums = factory.createAllCriteriumWrappers(SecuredActions.findByFormalName(getSecuredAction()));
        return criteriums;
    }

    public List<RightCriteriumParams> getRightCriteriumParams() {
        RightCriteriumParams[] allParams = this.rightsManager.findAllParams();
        return Arrays.asList(allParams);    
    }
    
    public Actions getAction() {
        String parameter = this.requestProvider.get().getParameter(ACTION);
        if (parameter == null) return Actions.create;
        else return Actions.valueOf(parameter);
    }
    
    
    public boolean isJustCreated() {
        return getAction() == Actions.create;
    }
    
    

    public static enum Actions {
        edit {

            @Override
            public String getName() {
                return name();
            }
        },
        create {

            @Override
            public String getName() {
                return name();
            }
            
        };
        
        public abstract String getName();
    }
}
