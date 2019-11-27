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

import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import cz.incad.kramerius.rest.api.exceptions.CreateException;
import cz.incad.kramerius.rest.api.exceptions.DeleteException;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.rest.api.utils.dbfilter.DbFilterUtils.FormalNamesMapping;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.database.TypeOfOrdering;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.database.Offset;
import cz.incad.kramerius.utils.database.Ordering;
import cz.incad.kramerius.utils.database.SQLFilter;
import cz.incad.kramerius.utils.database.SQLFilter.TypesMapping;


/**
 * Endpoint for manipulation with roles
 * @author pavels
 */
@Path("/v5.0/admin/roles")
public class RolesResource {

	@Inject
	UserManager userManager;
	
    @Inject
    Provider<User> userProvider;
    
    @Inject
    IsActionAllowed actionAllowed;

	

    public static FormalNamesMapping FNAMES = new FormalNamesMapping(); static {
    	FNAMES.map("id","group_id");
    	FNAMES.map("name","gname");
	}; 

	public static TypesMapping TYPES = new TypesMapping(); static {
		TYPES.map("group_id", new SQLFilter.IntegerConverter());
		TYPES.map("gname", new SQLFilter.StringConverter());
	}

    @GET
    @Path("{id:[0-9]+}")
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    public Response role(@PathParam("id") String roleId) {
    	try {
            if (permit(this.userProvider.get())) {
            	Role role = this.userManager.findRole(Integer.parseInt(roleId));
            	if (role != null) {
            		return Response.ok().entity(roleToJSON(role).toString()).build();
            	} else throw new ObjectNotFound("cannot find role '"+roleId+"'");
            } else {
            	throw new ActionNotAllowed("not allowed");
            }
        } catch (JSONException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    
    @GET
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    public Response getRoles(

            @QueryParam("name") String filterName,
            @QueryParam("offset") String filterOffset,
            @QueryParam("resultSize") String filterResultSize,
            @QueryParam("ordering") String filterOrdering,
            @QueryParam("typefordering") @DefaultValue("ASC")String typeofordering) {

    	if (permit(this.userProvider.get())) {
        	try {
                Offset offset = null;
                if (StringUtils.isAnyString(filterOffset)) {
                	offset = new Offset(filterOffset, filterResultSize);
                }
                Ordering ordering = null;
                if (StringUtils.isAnyString(filterOffset)) {
                	ordering = new Ordering("group_id","gname").select(transform(FNAMES,filterOrdering));
                }
                TypeOfOrdering type = null;
                if (StringUtils.isAnyString(typeofordering)) {
                	type = TypeOfOrdering.valueOf(typeofordering);
                }
                
                Map<String, String> filterMap = new HashMap<String, String>(); {
                    if (StringUtils.isAnyString(filterName)) filterMap.put(transform(FNAMES, "name"), filterName);
                };
                SQLFilter filter = simpleFilter(filterMap, TYPES);

                JSONArray jsonArray = new JSONArray();
                List<Role> roles = this.userManager.filterRoles(ordering,type,offset,filter);
                for (Role r : roles) {
                    jsonArray.put(roleToJSON(r));
                }
                return Response.ok().entity(jsonArray.toString()).build();
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage());
            }
    	} else {
    		throw new ActionNotAllowed("not allowed");
    	}
    }

    
    @DELETE
    @Path("{id:[0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id")String id){
    	if (permit(this.userProvider.get())) {
        	try {
                try {
                	Role r = this.userManager.findRole(Integer.parseInt(id));
                	if (r != null) {
                		this.userManager.removeRole(r);
                        return Response.ok().entity(roleToJSON(r).toString()).build();
                	} else throw new ObjectNotFound("cannot find role '"+id+"'");
                } catch (SQLException e) {
                	throw new DeleteException(e.getMessage());
                }
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage());
            }
    	} else {
    		throw new ActionNotAllowed("not allowed");    		
    	}
    }

    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(JSONObject uOptions) throws JSONException {
    	if (permit(this.userProvider.get())) {
        	try {
    			Role role = createRoleFromJSON(uOptions);
    			this.userManager.insertRole(role);
                URI uri = UriBuilder.fromResource(UsersResource.class).path("").build();
                return Response.created(uri).entity(roleToJSON(role).toString()).build();
        	} catch (SQLException e) {
        		throw new CreateException(e.getMessage());
        	}
    	} else throw new ActionNotAllowed("not allowed");
    }

	public static JSONObject roleToJSON(Role role) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", role.getName());
		json.put("id", role.getId());
		return json;
	}

	public static Role createRoleFromJSON(JSONObject uOptions) throws JSONException {
		int id = uOptions.getInt("id");
		String gname = uOptions.getString("name");
		Role r = new RoleImpl(id, gname, -1);
		return r;
	}
	
    boolean permit(User user) {
    	if (user != null)
    		return  this.actionAllowed.isActionAllowed(user,SecuredActions.USERSADMIN.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH);
    	else 
    		return false;
    }

}
