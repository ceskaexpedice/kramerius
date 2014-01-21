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
package cz.incad.kramerius.rest.api.k5.admin.vc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.virtualcollections.VirtualCollection;
import cz.incad.kramerius.virtualcollections.VirtualCollectionsManager;

@Path("/v5.0/k5/admin/vc")
public class VirtualCollectionsResource {

	public static final Logger LOGGER = Logger.getLogger(VirtualCollectionsResource.class.getName());
	
	@Inject
	@Named("securedFedoraAccess")
	FedoraAccess fedoraAccess;
	

	@Inject
	IsActionAllowed actionAllowed;
	
	@Inject
	Provider<User> userProvider;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON+ ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
	public Response post(JSONObject jsonObj) {
		if (permit(this.userProvider.get())) {
			try {
				String createdPID = VirtualCollectionsManager.create(fedoraAccess);
				VirtualCollection vc = findVirtualCollection(this.fedoraAccess,createdPID);
				if (vc != null) {
					String label = jsonObj.getString("label");
					boolean canLeaveFlag = jsonObj.getBoolean("canLeave");
					VirtualCollectionsManager.modify(createdPID, label, canLeaveFlag, fedoraAccess);
					VirtualCollection newVc = findVirtualCollection(this.fedoraAccess, createdPID);
					return Response.ok().entity(virtualCollectionTOJSON(newVc).toString()).build();
				} else {
					throw new ObjectNotFound("cannot find virtual collection '"+createdPID+"'");
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE,e.getMessage(),e);
				throw new GenericApplicationException(e.getMessage());
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,e.getMessage(),e);
				throw new GenericApplicationException(e.getMessage());
			}
		} else throw new ActionNotAllowed("action is not allowed");
	}	

	@PUT
	@Consumes(MediaType.APPLICATION_JSON+ ";charset=utf-8")
	@Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
	public Response put(JSONObject jsonObj) {
		if (permit(this.userProvider.get())) {
			try {
				String pid = jsonObj.getString("pid");
				VirtualCollection vc = findVirtualCollection(this.fedoraAccess,pid);
				if (vc != null) {
					String label = jsonObj.getString("label");
					boolean canLeaveFlag = jsonObj.getBoolean("canLeave");
					VirtualCollectionsManager.modify(pid, label, canLeaveFlag, fedoraAccess);
					JSONObject jsonObject = virtualCollectionTOJSON(findVirtualCollection(this.fedoraAccess,pid));
					return Response.ok().entity(jsonObject.toString()).build();
				} else {
					throw new ObjectNotFound("cannot find virtual collection '"+pid+"'");
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE,e.getMessage(),e);
				throw new GenericApplicationException(e.getMessage());
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,e.getMessage(),e);
				throw new GenericApplicationException(e.getMessage());
			}
		} else throw new ActionNotAllowed("action is not allowed");
	}	
	

	@DELETE
	@Path("{pid}")
	@Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
	public Response delete(@PathParam("pid")String pid) {
		if (permit(this.userProvider.get())) {
			try {
				VirtualCollection vc = findVirtualCollection(this.fedoraAccess,pid);
				if (vc != null) {
					JSONObject jsonObj = virtualCollectionTOJSON(vc);
					jsonObj.put("deleted", true);
					return Response.ok().entity(jsonObj.toString()).build();
				} else {
					throw new ObjectNotFound("cannot find vc '"+pid+"'");
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,e.getMessage(),e);
				throw new GenericApplicationException(e.getMessage());
			}
		} else throw new ActionNotAllowed("action is not allowed");
	}	

	@GET
	@Path("{pid}")
    @Consumes
	@Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
	public Response oneVirtualCollection(@PathParam("pid")String pid) {
		if (permit(this.userProvider.get())) {
			try {
				VirtualCollection vc = findVirtualCollection(this.fedoraAccess, pid);
				if (vc != null) {
					return Response.ok().entity(virtualCollectionTOJSON(vc)).build();
				} else {
					throw new ObjectNotFound("cannot find vc '"+pid+"'");
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(),e);
				throw new GenericApplicationException(e.getMessage());
			}
		} else throw new ActionNotAllowed("action is not allowed");
	}

	
	@GET
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
	public Response get() {
		if (permit(this.userProvider.get())) {
			try {
				List<VirtualCollection> vcs = VirtualCollectionsManager.getVirtualCollections(fedoraAccess, new ArrayList<String>());
				JSONArray jsonArr = new JSONArray();
				for (VirtualCollection vc : vcs) {
					jsonArr.add(virtualCollectionTOJSON(vc));
				}
				return Response.ok().entity(jsonArr.toString()).build();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,e.getMessage(),e);
				throw new GenericApplicationException(e.getMessage());
			}
		} else throw new ActionNotAllowed("action is not allowed");
	}

	
	public static JSONObject virtualCollectionTOJSON(VirtualCollection vc) {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("pid", vc.getPid());
		jsonObj.put("label", vc.getLabel());
		jsonObj.put("canLeave", vc.isCanLeave());
		JSONObject jsonMap = new JSONObject();
		Map<String, String> descMAp = vc.getDescriptionsMap();
		for (String	 k : descMAp.keySet()) { jsonMap.put(k, descMAp.get(k)); }
		jsonObj.put("descs", jsonMap);
		return jsonObj;
	}

	public static VirtualCollection findVirtualCollection(FedoraAccess fa, String pid)
			throws Exception {
		VirtualCollection vc = null;
		List<VirtualCollection> vcs = VirtualCollectionsManager.getVirtualCollections(fa, new ArrayList<String>());
		for (VirtualCollection v : vcs) {
			if (v.getPid().equals(pid)) {
				vc = v;
				break;
			}
		}
		return vc;
	}	

    boolean permit(User user) {
    	if (user != null)
    		return  this.actionAllowed.isActionAllowed(user,SecuredActions.VIRTUALCOLLECTION_MANAGE.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH);
    	else 
    		return false;
    }

}
