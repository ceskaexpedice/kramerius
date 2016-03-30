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
package cz.incad.kramerius.rest.api.k5.admin.users;

import static cz.incad.kramerius.rest.api.utils.dbfilter.DbFilterUtils.simpleFilter;
import static cz.incad.kramerius.rest.api.utils.dbfilter.DbFilterUtils.transform;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Provider;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.api.exceptions.CreateException;
import cz.incad.kramerius.rest.api.exceptions.DeleteException;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.exceptions.UpdateException;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.rest.api.utils.dbfilter.DbFilterUtils.FormalNamesMapping;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.database.TypeOfOrdering;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.utils.PasswordDigest;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.database.Offset;
import cz.incad.kramerius.utils.database.Ordering;
import cz.incad.kramerius.utils.database.SQLFilter;
import cz.incad.kramerius.utils.database.SQLFilter.TypesMapping;

/**
 * Endpoint for users manipulation
 * 
 * @author pavels
 */
@Path("/v5.0/admin/users")
public class UsersResource {

    @Inject
    UserManager userManager;

    @Inject
    Provider<User> userProvider;

    @Inject
    IsActionAllowed actionAllowed;

    // public static final String[] COLS = new String[]
    // {"user_id","name","surname","loginname"};
    public static FormalNamesMapping FNAMES = new FormalNamesMapping();
    static {
        FNAMES.map("id", "user_id");
        FNAMES.map("lname", "loginname");
        FNAMES.map("firstname", "fistname");
        FNAMES.map("surname", "surname");
    };

    public static TypesMapping TYPES = new TypesMapping();
    static {
        TYPES.map("user_id", new SQLFilter.IntegerConverter());
        TYPES.map("loginname", new SQLFilter.StringConverter());
        TYPES.map("fistname", new SQLFilter.StringConverter());
        TYPES.map("surname", new SQLFilter.StringConverter());
    }

    @GET
    @Path("{id:[0-9]+}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response role(@PathParam("id") String uid) {
        if (permit(this.userProvider.get())) {
            try {
                User ur = this.userManager.findUser(Integer.parseInt(uid));
                if (ur != null) {
                    return Response.ok().entity(userToJSON(ur).toString()).build();
                } else
                    throw new ObjectNotFound("cannot find role '" + uid + "'");
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getUsers(

            @QueryParam("lname") String filterLoginName,
            @QueryParam("firstname") String filterFirstname,
            @QueryParam("surname") String filterSurname,
            @QueryParam("offset") String filterOffset,
            @QueryParam("resultSize") String filterResultSize,
            @QueryParam("ordering") String filterOrdering,
            @QueryParam("typefordering") @DefaultValue("ASC") String typeofordering) {

        if (permit(this.userProvider.get())) {
            try {
                Offset offset = null;
                if (StringUtils.isAnyString(filterOffset)) {
                    offset = new Offset(filterOffset, filterResultSize);
                }
                Ordering ordering = null;
                if (StringUtils.isAnyString(filterOffset)) {
                    ordering = new Ordering("user_id", "loginname", "fistname",
                            "surname").select(transform(FNAMES, filterOrdering));
                }
                TypeOfOrdering type = null;
                if (StringUtils.isAnyString(typeofordering)) {
                    type = TypeOfOrdering.valueOf(typeofordering);
                }

                Map<String, String> filterMap = new HashMap<String, String>();
                {
                    if (StringUtils.isAnyString(filterLoginName))
                        filterMap.put(transform(FNAMES, "lname"), filterLoginName);
                    if (StringUtils.isAnyString(filterFirstname))
                        filterMap.put(transform(FNAMES, "firstname"),
                                filterFirstname);
                    if (StringUtils.isAnyString(filterSurname))
                        filterMap.put(transform(FNAMES, "surname"), filterSurname);
                }
                ;
                SQLFilter filter = simpleFilter(filterMap, TYPES);

                JSONArray jsonArray = new JSONArray();
                List<User> users = this.userManager.filterUsers(ordering, type,
                        offset, filter);
                for (User user : users) {
                    jsonArray.put(userToJSON(user));
                }

                return Response.ok().entity(jsonArray.toString()).build();
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }


    @PUT
    @Path("{id:[0-9]+}/password")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putPassword(@PathParam("id") String id, JSONObject uOptions) {
        if (permit(this.userProvider.get())) {
            try {
                User u = userManager.findUser(Integer.parseInt(id));
                if (u != null) {
                    if (!uOptions.has("password")) {
                        throw new IllegalStateException("expecting password key");
                    }
                    String pswd = uOptions.getString("password");
                    pswd = PasswordDigest.messageDigest(pswd);
                    this.userManager.saveNewPassword(u.getId(), pswd);
                    u = this.userManager.findUser(u.getId());
                    return Response.ok().entity(userToJSON(u).toString())
                            .build();
                } else {
                    throw new ObjectNotFound("cannot find user '" + id + "'");
                }
            } catch (SQLException e) {
                throw new UpdateException(e.getMessage(), e);
            } catch (NoSuchAlgorithmException e) {
                throw new UpdateException(e.getMessage(), e);
            } catch (UnsupportedEncodingException e) {
                throw new UpdateException(e.getMessage(), e);
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }
    
    @PUT
    @Path("{id:[0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(@PathParam("id") String id, JSONObject uOptions) {
        if (permit(this.userProvider.get())) {
            try {
                User u = userManager.findUser(Integer.parseInt(id));
                if (u != null) {
                    // TODO: Update firstname, surname !!
                    try {
                        JSONArray roles = uOptions.getJSONArray("roles");
                        List<String>rList = new ArrayList<String>();
                        for (int i = 0,ll=roles.length(); i < ll; i++) {
                            Object object = roles.get(i);
                            if (object instanceof String) {
                                rList.add(roles.getString(i));
                            } else if (object instanceof JSONObject) {
                                JSONObject jsonObj = (JSONObject) object;
                                rList.add(jsonObj.getString("name"));
                            }
                        }
                        
                        this.userManager.changeRoles(u, rList);
                        u = this.userManager.findUser(u.getId());
                        return Response.ok().entity(userToJSON(u).toString())
                                .build();
                    } catch (JSONException e) {
                        throw new GenericApplicationException(e.getMessage());
                    }
                } else {
                    throw new ObjectNotFound("cannot find user '" + id + "'");
                }
            } catch (SQLException e) {
                throw new UpdateException(e.getMessage(), e);
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }

    
    
    @DELETE
    @Path("{id:[0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        if (permit(this.userProvider.get())) {
            try {
                User u = this.userManager.findUser(Integer.parseInt(id));
                if (u != null) {
                    try {
                        User adminUser = this.userManager
                                .findUserByLoginName("krameriusAdmin");
                        if (u.getId() == adminUser.getId()) {
                            // it is not allowed delete kramerius admin
                            throw new ActionNotAllowed("not allowed");
                        }
                        this.userManager.deleteUser(u);
                        JSONObject json = userToJSON(u);
                        json.put("deleted", true);
                        return Response.ok().entity(json.toString()).build();
                    } catch (JSONException e) {
                        throw new GenericApplicationException(e.getMessage());
                    }
                } else {
                    throw new ObjectNotFound("cannot find user '" + id + "'");
                }
            } catch (SQLException e) {
                throw new DeleteException("cannot find user '" + id + "'");
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(JSONObject uOptions) {
        if (permit(this.userProvider.get())) {
            try {
                User user = createUserFromJSON(uOptions);
                if (!uOptions.has("password")) {
                    throw new IllegalStateException("expecting password key");
                }
                
                User userByLName = this.userManager.findUserByLoginName(user.getLoginname());
                if (userByLName != null) {
                    throw new BadRequestException("user with login name '"+user.getLoginname()+"'");
                }
                
                String pswd = uOptions.getString("password");
                this.userManager.insertUser(user, pswd);
                this.userManager.activateUser(user);
                URI uri = UriBuilder.fromResource(UsersResource.class).path("")
                        .build();
                return Response.created(uri)
                        .entity(userToJSON(user).toString()).build();
            } catch (SQLException e) {
                throw new CreateException(e.getMessage(), e);
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }

    public static JSONObject userToJSON(User user) throws JSONException {
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
                jsonArr.put(json);
            }
            jsonObj.put("roles", jsonArr);
        }
        return jsonObj;
    }

    public static User createUserFromJSON(JSONObject uOptions) throws JSONException {
        String lname = uOptions.getString("lname");
        String fname = uOptions.getString("firstname");
        String sname = uOptions.getString("surname");

        int id = -1;
        if (uOptions.has("id")) {
            uOptions.getInt("id");
        }

        UserImpl u = new UserImpl(id, fname, sname, lname, -1);
        if (uOptions.has("roles")) {
            List<Role> rlist = new ArrayList<Role>();
            JSONArray jsonArr = uOptions.getJSONArray("roles");
            for (int i = 0,ll=jsonArr.length(); i < ll; i++) {
                JSONObject jsonObj = (JSONObject) jsonArr.get(i);
                rlist.add(RolesResource.createRoleFromJSON(jsonObj));
            }
            u.setGroups(rlist.toArray(new Role[rlist.size()]));
        }

        return u;
    }

    boolean permit(User user) {
        if (user != null)
            return this.actionAllowed.isActionAllowed(user,
                    SecuredActions.USERSADMIN.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(), null,
                    ObjectPidsPath.REPOSITORY_PATH);
        else
            return false;
    }

}
