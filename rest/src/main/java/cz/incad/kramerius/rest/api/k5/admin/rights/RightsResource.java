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
package cz.incad.kramerius.rest.api.k5.admin.rights;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import javax.ws.rs.core.UriBuilderException;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.admin.utils.LicenseUtils;
import cz.incad.kramerius.security.labels.Label;
import cz.incad.kramerius.security.labels.LabelsManager;
import cz.incad.kramerius.security.labels.LabelsManagerException;
import cz.incad.kramerius.security.utils.SortingRightsUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.RightCriteriumParamsImpl;
import cz.incad.kramerius.security.impl.RightImpl;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.service.ResourceBundleService;

/**
 * Rights end point
 * @author pavels
 */
@Path("/v5.0/admin/rights")
public class RightsResource {

    @Inject
    ResourceBundleService bundleService;
    
    @Inject
    Provider<Locale> localesProvider;

    @Inject
    Provider<User> userProvider;

    @Inject
    RightsManager rightsManager;
    
    @Inject
    RightCriteriumWrapperFactory critFactory;

    @Inject
    RightsResolver rightsResolver;

    @Inject
    LabelsManager labelsManager;

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;


    /**
     * Delete right with given id
     * @param id Right id
     * @return JSON of deleted right
     */
    @DELETE
    @Path("{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response delete(@PathParam("id") String id) {
        if (permit(this.userProvider.get())) {
            Right r = this.rightsManager.findRightById(Integer.parseInt(id));
            if (r != null) {
                try {
                    this.rightsManager.deleteRight(r);
                    JSONObject jsonRet = rightsToJSON(r);
                    jsonRet.put("deleted", true);
                    return Response.ok().entity(jsonRet.toString()).build();
                } catch (SQLException e) {
                    throw new GenericApplicationException(e.getMessage(), e);
                } catch (JSONException e) {
                    throw new GenericApplicationException(e.getMessage(), e);
                }
            } else throw new ObjectNotFound("cannot find right with id "+id);
        } else throw new ActionNotAllowed("action is not allowed");
    }

    /**
     * Creates new right
     * @param json JSON represents right to be created
     * @return Created right in JSON representation
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response insert(JSONObject json) {
        if (permit(this.userProvider.get())) {
            try {
                Right r = this.rightFromJSON(json);		
                if (r != null)  {
                    try {
                        int rid = this.rightsManager.insertRight(r);
                        Right nr = this.rightsManager.findRightById(rid);
                        URI uri = UriBuilder.fromResource(RightsResource.class).path("{id}").build(nr.getId());
                        return Response.created(uri).entity(rightsToJSON(nr).toString()).build();
                    } catch (IllegalArgumentException e) {
                        throw new GenericApplicationException(e.getMessage(), e);
                    } catch (UriBuilderException e) {
                        throw new GenericApplicationException(e.getMessage(), e);
                    } catch (SQLException e) {
                        throw new GenericApplicationException(e.getMessage(), e);
                    } catch (JSONException e) {
                        throw new GenericApplicationException(e.getMessage(), e);
                    }
                } else {
                    throw new GenericApplicationException("cannot insert right!");
                }
            } catch (JSONException | LabelsManagerException e) {
                throw new GenericApplicationException(e.getMessage(), e);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }

    /**
     * Update right
     * @param jsonObject
     * @return
     */
    @PUT
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response update(JSONObject jsonObject) {
        if (permit(this.userProvider.get())) {
            try {
                Right r = this.rightFromJSON(jsonObject);
                if (r != null) {
                    try {
                        this.rightsManager.updateRight(r);
                        Right nr = this.rightsManager.findRightById(r.getId());
                        return Response.ok().entity(rightsToJSON(nr).toString()).build();
                    } catch (JSONException e) {
                        throw new GenericApplicationException(e.getMessage(), e);
                    }
                } else {
                    throw new ObjectNotFound("cannot find right for '"+jsonObject+"'");
                }
            } catch (SQLException | JSONException | LabelsManagerException e) {
                throw new GenericApplicationException("cannot insert right!");
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }


    @GET
    @Path("{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response right( @PathParam("id")String id) {
        if (permit(this.userProvider.get())) {
            Right r = this.rightsManager.findRightById(Integer.parseInt(id));
            if (r != null) {
                try {
                    return Response.ok().entity(rightsToJSON(r).toString()).build();
                } catch (JSONException e) {
                    throw new GenericApplicationException(e.getMessage(), e);
                }
            } else throw new ObjectNotFound("cannot find right '"+id+"'");
        } else throw new ActionNotAllowed("action is not allowed");
    }

    
    @GET
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response rights( @QueryParam("pid")String pid, @QueryParam("action")String action) {
        if (permit(this.userProvider.get())) {

            try {
                List<String> roles = new ArrayList<String>();

                Right[] rights = null;

                if (pid != null && action != null) {
                    rights = this.rightsManager.findAllRights(new String[] {pid}, action);
                    if (SpecialObjects.isSpecialObject(pid) && pid.equals("uuid:1")) {
                        rights = SortingRightsUtils.sortRights(rights, new ObjectPidsPath(pid));
                    } else {
                        //oppps
                        ObjectPidsPath[] pidPaths = solrAccess.getPidPaths(pid);
                        // asi nebude jine trideni pro jinou pidpath; vyberemme prvni
                        if (pidPaths.length > 0) {
                            rights = SortingRightsUtils.sortRights(rights,pidPaths[0].injectRepository());
                        }
                    }
                } else {
                    rights = this.rightsManager.findRights(new String[0],pid != null ? new String[]{pid} : new String[0],  action != null ? new String[] {action} : new String[0], roles.toArray(new String[roles.size()]));
                }


                JSONArray jsonArr = new JSONArray();
                for (Right r : rights) {
                    JSONObject json = rightsToJSON(r);
                    jsonArr.put(json);
                }
                
                return Response.ok().entity(jsonArr.toString()).build();
            } catch (JSONException  | IOException e) {
                throw new GenericApplicationException(e.getMessage(), e);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }

    @DELETE
    @Path("params/{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response deleteParam(@PathParam("id")String id) {
        if (permit(this.userProvider.get())) {
            try {
                int id2 = Integer.parseInt(id);
                RightCriteriumParams params = this.rightsManager.findParamById(id2);
                if (params != null) {
                    try {
                        this.rightsManager.deleteRightCriteriumParams(id2);
                        JSONObject jsonObject = paramToJSON(params);
                        jsonObject.put("deleted", true);
                        return Response.ok().entity(jsonObject.toString()).build();
                    } catch (JSONException e) {
                        throw new GenericApplicationException(e.getMessage(), e);
                    }
                } else {
                    throw new ObjectNotFound("cannot find param '"+id+"'");
                }
            } catch (NumberFormatException e) {
                throw new GenericApplicationException(e.getMessage());
            } catch (SQLException e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }

    @POST
    @Path("params")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response createParam(JSONObject jsonObj) {
        if (permit(this.userProvider.get())) {
            try {
                RightCriteriumParams params = paramsFromJSON(jsonObj);
                int id = this.rightsManager.insertRightCriteriumParams(params);
                RightCriteriumParams[] p = this.rightsManager.findAllParams();
                for (RightCriteriumParams rp : p) {
                    if (rp.getId() == id)  {
                        return Response.ok().entity(paramToJSON(rp).toString()).build();
                    }
                }
                throw new GenericApplicationException("cannot find created params '"+id+"'");
            } catch (SQLException e) {
                throw new GenericApplicationException(e.getMessage());
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage(), e);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }


    @PUT
    @Path("params")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response updateParam(JSONObject jsonObj) {
        if (permit(this.userProvider.get())) {
            try {
                RightCriteriumParams params = paramsFromJSON(jsonObj);
                int id = params.getId();
                this.rightsManager.updateRightCriteriumParams(params);
                RightCriteriumParams[] p = this.rightsManager.findAllParams();
                for (RightCriteriumParams rp : p) {
                    if (rp.getId() == id)  {
                        return Response.ok().entity(paramToJSON(rp).toString()).build();
                    }
                }
                throw new GenericApplicationException("cannot find created params '"+id+"'");
            } catch (SQLException e) {
                throw new GenericApplicationException(e.getMessage());
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage(), e);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }


    @GET
    @Path("params/{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response params(@PathParam("id")String id) {
        if (permit(this.userProvider.get())) {
            try {
                int iId = Integer.parseInt(id);
                RightCriteriumParams[] params = this.rightsManager.findAllParams();
                for (RightCriteriumParams rp : params) {
                    if (rp.getId() == iId) {
                        return Response.ok().entity(paramToJSON(rp).toString()).build();
                    }
                }
                throw new ObjectNotFound("cannot find param '"+id+"'");
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage(), e);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }




    @GET
    @Path("params")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response params() {
        if (permit(this.userProvider.get())) {
            try {
                RightCriteriumParams[] params = this.rightsManager.findAllParams();
                JSONArray jsonArr = new JSONArray();
                for (RightCriteriumParams rp : params) {
                    JSONObject jsonObj = paramToJSON(rp);
                    jsonArr.put(jsonObj);
                }
                return Response.ok().entity(jsonArr.toString()).build();
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage(), e);
            }
		} else throw new ActionNotAllowed("action is not allowed");
	}




    @GET
    @Path("criteria")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response criteria() {
        if (permit(this.userProvider.get())) {
            try {

                JSONObject objects = new JSONObject();
                List<RightCriteriumWrapper> allCriteriumWrappers = critFactory.createAllCriteriumWrappers();
                allCriteriumWrappers.stream().forEach(c-> {
                    JSONObject critObject = new JSONObject();
                    critObject.put("paramsNecessary", c.getRightCriterium().isParamsNecessary());
                    critObject.put("rootLevelCriterum", c.getRightCriterium().isRootLevelCriterum());
                    critObject.put("isLabelAssignable", c.getRightCriterium().isLabelAssignable());

                    JSONArray actionsArray = new JSONArray();
                    Arrays.stream(c.getRightCriterium().getApplicableActions()).map(SecuredActions::getFormalName).forEach(actionsArray::put);
                    critObject.put("applicableActions", actionsArray);


                objects.put(c.getRightCriterium().getQName(), critObject);
                });

                return Response.ok().entity(objects.toString()).build();
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage(), e);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }



    @GET
    @Path("licenses")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response licenses() {
        if (permit(this.userProvider.get())) {
            try {
                List<Label> labels = this.labelsManager.getLabels();
                JSONArray jsonArray = new JSONArray();
                labels.stream().forEach(l-> {
                    JSONObject labelObject = LicenseUtils.licenseToJSON(l);
                    jsonArray.put(labelObject);
                });
                return Response.ok().entity(jsonArray.toString()).build();
            } catch (JSONException  | LabelsManagerException e) {
                throw new GenericApplicationException(e.getMessage(), e);
            }
        } else throw new ActionNotAllowed("action is not allowed");
    }

    private JSONObject userToJSON(AbstractUser au) throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("id", au.getId());
		if (au instanceof Role) {
			jsonObj.put("name", ((Role)au).getName());
		}
		return jsonObj;
	}
	private JSONObject criteriumToJSON(RightCriteriumWrapper rcw) throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("qname", rcw.getRightCriterium().getQName());
		jsonObj.put("params", paramToJSON(rcw.getCriteriumParams()));

        Label label = rcw.getLabel();
        if (label != null) {
            jsonObj.put("label", label.getName());
        }

        return jsonObj;
	}	
	
	private JSONObject rightsToJSON(Right r) throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("id", r.getId());
		jsonObj.put("action", r.getAction());
		jsonObj.put("pid", r.getPid());

		if (r.getFixedPriority() > 0 || r.getFixedPriority() < 0) {
			jsonObj.put("fixedPriority", r.getFixedPriority());
		}
		jsonObj.put("role", userToJSON(r.getUser()));
		
		if (r.getCriteriumWrapper() != null)
			jsonObj.put("criterium", criteriumToJSON(r.getCriteriumWrapper()));
		
		return jsonObj;
	}

	private JSONObject paramToJSON(RightCriteriumParams rp) throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("id", rp.getId());
		jsonObj.put("description", rp.getShortDescription());
		
		Object[] objs = rp.getObjects();
		JSONArray objsArr = new JSONArray();
		for (int i = 0; i < objs.length; i++) {
			objsArr.put(objs[i].toString());
		}
		jsonObj.put("objects", objsArr);
		return jsonObj;
	}
	
	private Role roleFromJSON(JSONObject jsonObj) throws JSONException {
		int id = -1;
		if (jsonObj.has("id")) {
			id = jsonObj.getInt("id");
		}
		String rname = jsonObj.getString("name");
		RoleImpl rm = new RoleImpl(id, rname, -1);
		return rm;
	}

	private RightCriteriumParams paramsFromJSON(JSONObject jsonObj) throws JSONException {
		int id = -1;
		if (jsonObj.has("id")) {
			id = jsonObj.getInt("id");
		}
		RightCriteriumParams param = new RightCriteriumParamsImpl(id);
//		if (jsonObj.has("ldescription")) {
//			param.setLongDescription(jsonObj.getString("ldesc"));
//		}
		if (jsonObj.has("description")) {
			param.setShortDescription(jsonObj.getString("description"));
		}
		if (jsonObj.has("objects")) {
			List<Object> objs = new ArrayList<Object>();
			JSONArray jsonArr = jsonObj.getJSONArray("objects");
			for (int i = 0, ll = jsonArr.length(); i < ll; i++) {
				objs.add(jsonArr.get(i));
			}
			param.setObjects(objs.toArray());
		}
		return param;
	}
	
	private RightCriteriumWrapper rightCriteriumWrapper(JSONObject jsonObj) throws JSONException, LabelsManagerException {
		String qname = jsonObj.getString("qname");
		RightCriteriumWrapper wrapper = this.critFactory.createCriteriumWrapper(qname);
		if (jsonObj.has("params")) {
			wrapper.setCriteriumParams(paramsFromJSON(jsonObj.getJSONObject("params")));
		}

		if (jsonObj.has("label")) {
            Label label = this.labelsManager.getLabelByName(jsonObj.getString("label"));
            wrapper.setLabel(label);
        }

		return wrapper;
	}


    private Right rightFromJSON(JSONObject jsonObj) throws JSONException, LabelsManagerException {
		int id = -1;
		if (jsonObj.has("id")) {
			id = jsonObj.getInt("id");
		}
		String action = jsonObj.getString("action");
		String pid = jsonObj.getString("pid");
		Role role = roleFromJSON(jsonObj.getJSONObject("role"));
		RightCriteriumWrapper wrapper = null;
		if (jsonObj.has("criterium")){
			wrapper = rightCriteriumWrapper(jsonObj.getJSONObject("criterium"));
		}
		RightImpl rimpl = new RightImpl(id, wrapper, pid, action, role);
		if (jsonObj.has("fixedPriority")) {
			rimpl.setFixedPriority(jsonObj.getInt("fixedPriority"));
		}
		return rimpl;

	}
	
	boolean permit(User user) {
    	if (user != null)
    		return  this.rightsResolver.isActionAllowed(user,SecuredActions.ADMINISTRATE.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH).flag();
    	else 
    		return false;
    }

}
