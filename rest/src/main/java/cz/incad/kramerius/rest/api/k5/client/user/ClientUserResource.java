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
package cz.incad.kramerius.rest.api.k5.client.user;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.users.UserProfileManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Path("/k5/user")
public class UsersResource {


    @Inject
    UserProfileManager userProfileManager;

    @Inject
    IsActionAllowed isActionAllowed;
	
    @Inject
    Provider<User> userProvider;
    
    
	@GET
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response info() {
		User user = this.userProvider.get();
		if (user != null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("firstname", user.getFirstName());
			jsonObject.put("surname", user.getSurname());
			jsonObject.put("loginname", user.getLoginname());
			jsonObject.put("email", user.getEmail());
			JSONArray jsonArr = new JSONArray();
			Role[] roles = user.getGroups();
			for (Role r : roles) {
				jsonArr.add(jsonRole(r));
			}
			if (roles.length > 0) {
				jsonObject.put("roles", jsonArr);
			}
			return Response.ok().entity(jsonObject.toString()).build();
		} else {
			return Response.ok().entity("{}").build();
		}
	}
	

	private Object jsonRole(Role r) {
		JSONObject jsonObj = new JSONObject();
		String rolename = r.getName();
		jsonObj.put("name", rolename);
		return jsonObj;
	}


	@GET
	@Path("profile")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response profile() {
		User user = this.userProvider.get();
		UserProfile profile = this.userProfileManager.getProfile(user);
		return Response.ok().entity(profile.getRawData()).build();
	}

	@POST
	@Path("profile")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    @Consumes(MediaType.APPLICATION_JSON)
	public Response postProfile(JSONObject rawdata) {
		User user = this.userProvider.get();
        UserProfile profile = this.userProfileManager.getProfile(user);
        profile.setJSONData(rawdata);
		this.userProfileManager.saveProfile(user, profile);
		return Response.ok().entity(profile).build();
	}
}




