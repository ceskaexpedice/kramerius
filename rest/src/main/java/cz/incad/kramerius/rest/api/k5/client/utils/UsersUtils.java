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
package cz.incad.kramerius.rest.api.k5.client.utils;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import cz.incad.kramerius.rest.api.k5.admin.users.RolesResource;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;

public class UsersUtils {

    public static JSONObject userToJSON(User user) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("lname", user.getLoginname());
        jsonObj.put("firstname", user.getFirstName());
        jsonObj.put("surname", user.getSurname());
        jsonObj.put("id", user.getId());

        JSONArray jsonArr = new JSONArray();
        Role[] roles = user.getGroups();
        if (roles != null) {
            for (Role r : roles) {
                JSONObject json = RolesResource.roleToJSON(r);
                jsonArr.add(json);
            }
            jsonObj.put("roles", jsonArr);
        }
        return jsonObj;
    }

    public static User createUserFromJSON(JSONObject uOptions) {
        String lname = uOptions.getString("lname");
        String fname = uOptions.getString("firstname");
        String sname = uOptions.getString("surname");

        int id = -1;
        if (uOptions.containsKey("id")) {
            uOptions.getInt("id");
        }

        UserImpl u = new UserImpl(id, fname, sname, lname, -1);
        if (uOptions.containsKey("roles")) {
            List<Role> rlist = new ArrayList<Role>();
            JSONArray jsonArr = uOptions.getJSONArray("roles");
            for (Object obj : jsonArr) {
                JSONObject jsonObj = (JSONObject) obj;
                rlist.add(RolesResource.createRoleFromJSON(jsonObj));
            }
            u.setGroups(rlist.toArray(new Role[rlist.size()]));
        }

        return u;
    }

}
