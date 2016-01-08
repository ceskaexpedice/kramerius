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
package cz.incad.kramerius.rest.api.k5.client.authentication;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.security.User;

//TODO: delete
@Path("/auth")
public class AuthenticationResource {

	@Inject
	Provider<User> provider;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response auth() {
		try {
            User user = provider.get();
            if (user != null) {
            	JSONObject jsonObj = new JSONObject();
            	jsonObj.put("loginname", user.getLoginname());
            	jsonObj.put("firstname", user.getFirstName());
            	jsonObj.put("surname", user.getSurname());
            	return Response.ok().entity(jsonObj.toString()).build();
            } else {
            	return Response.ok().entity(new JSONObject().toString()).build();
            }
        } catch (JSONException e) {
            throw new GenericApplicationException(e.getMessage());
        }
	}
}
