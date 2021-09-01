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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.DatabaseRightsManager;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTLabels;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.utils.UsersUtils;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.utils.PasswordDigest;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.users.UserProfileManager;

@Path("/v5.0/user")
public class ClientUserResource {

    public static final Logger LOGGER  = Logger.getLogger(ClientUserResource.class.getName());

    public static final String[] LABELS_CRITERIA = new String[]{
            "cz.incad.kramerius.security.impl.criteria.ReadDNNTLabels",
            "cz.incad.kramerius.security.impl.criteria.ReadDNNTLabelsIPFiltered"
    };

    @Inject
    UserProfileManager userProfileManager;

    @Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<User> userProvider;

    @Inject
    UserManager userManager;


    @Inject
    DatabaseRightsManager databaseRightsManager;


    @Inject
    RightCriteriumContextFactory ctxFactory;


    @Inject
    Provider<HttpServletRequest> provider;

    @GET
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response info() {
        try {
            User user = this.userProvider.get();
            // DNNT extension
            List<String> labels = findLabels(user);
            if (user != null) {
                return Response.ok().entity(UsersUtils.userToJSON(user,labels).toString())
                        .build();
            } else {
                return Response.ok().entity("{}").build();
            }
        } catch (JSONException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    private List<String> findLabels(User user) {
        // find all rights associated with current user and labels criteria
        Right[] rightsAsssignedWithLabels = this.databaseRightsManager.findAllRightByCriteriumNames(
                SecuredActions.READ.getFormalName(),
                LABELS_CRITERIA,
                user
        );
        // mock evaluate this criteria
        List<String> evaluatedObjects = new ArrayList<>();
        RightCriteriumContext ctx = this.ctxFactory.create(SpecialObjects.REPOSITORY.getPid(), ImageStreams.IMG_FULL.name(), user, this.provider.get().getRemoteHost(), IPAddressUtils.getRemoteAddress(this.provider.get(), KConfiguration.getInstance().getConfiguration()), null);
        Arrays.stream(rightsAsssignedWithLabels).forEach(right -> {
            try {
                EvaluatingResultState result = right.mockEvaluate(ctx, this.databaseRightsManager, DataMockExpectation.EXPECT_DATA_VAUE_EXISTS);
                if (result.equals(EvaluatingResultState.TRUE)) {
                    String name = right.getCriteriumWrapper().getLabel().getName();
                    evaluatedObjects.add(name);
                }
            } catch (RightCriteriumException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        });

        return evaluatedObjects.stream().filter(label-> {
            return  (Character.isAlphabetic(label.charAt(0)));
        }).collect(Collectors.toList());

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
    @Path("logout")
    public Response logout() {
        HttpServletRequest httpServletRequest = this.provider.get();
        httpServletRequest.getSession().invalidate();
        return Response.ok().entity("{}").build();
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
