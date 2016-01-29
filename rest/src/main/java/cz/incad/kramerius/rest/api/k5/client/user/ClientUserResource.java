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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.utils.UsersUtils;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.utils.PasswordDigest;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.users.UserProfileManager;

@Path("/v5.0/user")
public class ClientUserResource {

    @Inject
    UserProfileManager userProfileManager;

    @Inject
    IsActionAllowed isActionAllowed;

    @Inject
    Provider<User> userProvider;

    @Inject
    UserManager userManager;

    @GET
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response info() {
        try {
            User user = this.userProvider.get();
            if (user != null) {
                return Response.ok().entity(UsersUtils.userToJSON(user).toString())
                        .build();
            } else {
                return Response.ok().entity("{}").build();
            }
        } catch (JSONException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    @POST
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(JSONObject rawdata) {
        User user;
        try {
            user = this.userProvider.get();
            if (user != null && user.getId() != -1) {
                if (rawdata.has("pswd")) {
                    String newPswd = PasswordDigest.messageDigest(rawdata
                            .getString("pswd"));
                    this.userManager.saveNewPassword(user.getId(), newPswd);
                    return Response.ok()
                            .entity(UsersUtils.userToJSON(user).toString())
                            .build();
                } else {
                    throw new ObjectNotFound("cannot find user " + user.getId());
                }
            } else {
                throw new ObjectNotFound("cannot find user " + user.getId());
            }
        } catch (SQLException e) {
            throw new GenericApplicationException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new GenericApplicationException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new GenericApplicationException(e.getMessage());
        } catch (JSONException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    @GET
    @Path("profile")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response profile() {
        User user = this.userProvider.get();
        UserProfile profile = this.userProfileManager.getProfile(user);
        return Response.ok().entity(profile.getRawData()).build();
    }

    @POST
    @Path("profile")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postProfile(JSONObject rawdata) {
        User user = this.userProvider.get();
        UserProfile profile = this.userProfileManager.getProfile(user);
        profile.setJSONData(rawdata);
        this.userProfileManager.saveProfile(user, profile);
        return Response.ok().entity(profile.getJSONData().toString()).build();
    }
}
