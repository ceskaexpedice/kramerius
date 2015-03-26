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
package cz.incad.kramerius.client.kapi.auth.impl;

import java.util.Map;

import org.json.JSONObject;

import cz.incad.kramerius.client.kapi.auth.AdminUser;
import cz.incad.kramerius.client.kapi.auth.CallUserController;
import cz.incad.kramerius.client.kapi.auth.ClientUser;
import cz.incad.kramerius.client.kapi.auth.ProfileDelegator;
import cz.incad.kramerius.client.kapi.auth.User;

public class CallUserControllerImpl extends CallUserController {

    private AdminUser aUser;
    private ClientUser cUser;
    private ProfileDelegator pDelegator;

    private JSONObject jsonRepresention;
    private JSONObject profileJSONReprestation;
    
    @Override
    public void createCaller(String name, String pswd, Class<? extends User> clz, User.UserProvider userProvider) {
        if (clz.equals(AdminUser.class)) {
            this.aUser = new AdminUserImpl(name, pswd);
            this.aUser.setUserProvider(User.UserProvider.K5);
        } else if (clz.equals(ClientUser.class)) {
            CallUserController.clearCredentials(name);
            this.cUser = new ClientUserImpl(name, pswd);
            CallUserController.credentialsTable(name, pswd);
            this.cUser.setUserProvider(userProvider);
        } else if (clz.equals(ProfileDelegator.class)) {
            CallUserController.clearCredentials(name);
            this.pDelegator = new ProfileDelegatorImpl(name, pswd);
            CallUserController.credentialsTable(name, pswd);
            this.cUser.setUserProvider(userProvider);
        } else
            throw new IllegalArgumentException(
                    "cannot create caller for instance '" + clz + "'");
    }

    @Override
    public void createCaller(String name, String pswd,
            Class<? extends User> user) {
        this.createCaller(name, pswd, user, User.UserProvider.K5);
    }



    @Override
    public AdminUser getAdminCaller() {
        return this.aUser;
    }

    @Override
    public ClientUser getClientCaller() {
        this.check(this.cUser);
        return this.cUser;
    }

    @Override
    public ProfileDelegator getProfileDelegator() {
        this.check(this.pDelegator);
        return this.pDelegator;
    }

    @Override
    public JSONObject getUserJSONRepresentation() {
        this.check(this.cUser);
        return this.jsonRepresention;
    }

    public void setUserJSONRepresentation(JSONObject jsonRep) {
        this.jsonRepresention = jsonRep;
    }

    @Override
    public JSONObject getProfileJSONRepresentation() {
        return this.profileJSONReprestation;
    }

    public void setProfileJSONReprestation(JSONObject profileJSONReprestation) {
        this.profileJSONReprestation = profileJSONReprestation;
    }
    
    private void check(User u) {
        if (u != null) {
            if (!CallUserController.check(u.getUserName(), u.getPassword())) {
                this.cUser = null;
                this.pDelegator = null;
                this.jsonRepresention = null;
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aUser == null) ? 0 : aUser.hashCode());
        result = prime * result + ((cUser == null) ? 0 : cUser.hashCode());
        result = prime
                * result
                + ((jsonRepresention == null) ? 0 : jsonRepresention.hashCode());
        result = prime * result
                + ((pDelegator == null) ? 0 : pDelegator.hashCode());
        result = prime
                * result
                + ((profileJSONReprestation == null) ? 0
                        : profileJSONReprestation.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CallUserControllerImpl other = (CallUserControllerImpl) obj;
        if (aUser == null) {
            if (other.aUser != null)
                return false;
        } else if (!aUser.equals(other.aUser))
            return false;
        if (cUser == null) {
            if (other.cUser != null)
                return false;
        } else if (!cUser.equals(other.cUser))
            return false;
        if (jsonRepresention == null) {
            if (other.jsonRepresention != null)
                return false;
        } else if (!jsonRepresention.equals(other.jsonRepresention))
            return false;
        if (pDelegator == null) {
            if (other.pDelegator != null)
                return false;
        } else if (!pDelegator.equals(other.pDelegator))
            return false;
        if (profileJSONReprestation == null) {
            if (other.profileJSONReprestation != null)
                return false;
        } else if (!profileJSONReprestation
                .equals(other.profileJSONReprestation))
            return false;
        return true;
    }

    
    
}
