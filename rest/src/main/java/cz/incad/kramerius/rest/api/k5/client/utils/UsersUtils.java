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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.rest.apiNew.admin.v10.rights.RolesResource;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.impl.UserImpl;

public class UsersUtils {

    public static final String LNAME = "lname";
    public static final String FIRSTNAME = "firstname";
    public static final String SURNAME = "surname";
    public static final String ID = "id";
    public static final String ROLES = "roles";
    public static final String LICENSES = "licenses";
    public static final String SESSION = "session";

    public static final String AUTHENTICATED = "authenticated";
    public static final String UID="uid";
    public static final String NAME="name";


    public static JSONObject userToJSON(User user, boolean enhanceBySessionAttributes) throws JSONException {
        return  userToJSON(user, new ArrayList<>(),enhanceBySessionAttributes);
    }
    
    public static JSONObject userToJSON(User user, List<String> labels, boolean enhanceBySessionAttributes) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(UID, user.getLoginname());
        if (user.getId() != -1) {
            jsonObj.put(NAME, user.getFirstName() +" "+user.getSurname());
        }
        jsonObj.put(AUTHENTICATED, user.getId() != -1);

        JSONArray jsonArr = new JSONArray();
        Role[] roles = user.getGroups();
        if (roles != null) {
            for (Role r : roles) {
                jsonArr.put(r.getName());
            }
            jsonObj.put(ROLES, jsonArr);
        }

        JSONArray labelsArray = new JSONArray();
        labels.stream().forEach(labelsArray::put);
        jsonObj.put(LICENSES, labelsArray);
        

        // session attributes - Question 
        if (enhanceBySessionAttributes) {
            JSONObject jsonSessionAttributes = new JSONObject();
            user.getSessionAttributes().keySet().stream().forEach(key-> jsonSessionAttributes.put(key, user.getSessionAttributes().get(key)));
            jsonObj.put(SESSION, jsonSessionAttributes);
        }
        return jsonObj;
    }

    public static Pair<User, List<String>> userFromJSON(JSONObject json) throws JSONException {
    	if (json.has(AUTHENTICATED)) {
    		Map<String,String> session = new HashMap<>();
    		String loginName = json.optString(UID);
    		String firstName = null;
    		String surName = null;
    		List<String> licenses = new ArrayList<>();
    		List<Role> roles = new ArrayList<>();
    		JSONArray rolesJSONArray = json.optJSONArray(ROLES);
    		
    		if (rolesJSONArray != null) {
    			for (int i = 0; i < rolesJSONArray.length(); i++) {
					String optString = rolesJSONArray.optString(i);
					roles.add(new RoleImpl(optString));
				}
    		}
    		
    		if (json.has(SESSION)) {
    			JSONObject sessionAttrs = json.getJSONObject(SESSION);
    			sessionAttrs.keySet().forEach(key-> {
    				Object object = sessionAttrs.get((String) key);
    				session.put(key.toString(), object.toString());
    			});
    		}
    		
    		if (json.has(NAME)) {
    			String nameString = json.optString(NAME);
    			String[] split = nameString.split("\\s");
    			if (split.length > 1) {
    				firstName = split[0];
    				surName = split[1];
    			} else {
    				firstName = nameString;
    			}
    		}

    		if (json.has(LICENSES)) {
    			JSONArray licensesArray = json.getJSONArray(LICENSES);
    			for (int i = 0; i < licensesArray.length(); i++) {
    				licenses.add(licensesArray.getString(i));
				}
    		}
    		
    		UserImpl userImpl = new UserImpl(1, firstName != null ? firstName : "", surName !=  null ? surName : "", loginName, 0);
    		userImpl.setGroups(roles.toArray(new Role[roles.size()]));
    		session.entrySet().forEach(entry-> {
    			userImpl.addSessionAttribute(entry.getKey(), entry.getValue());
    		});

    		
    		return Pair.of(userImpl, licenses);
    		
    	} else return null;
    }
}
