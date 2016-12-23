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
import java.util.HashMap;
import java.util.Iterator;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.Collection.Description;
import cz.incad.kramerius.virtualcollections.CollectionException;
import cz.incad.kramerius.virtualcollections.CollectionUtils;
import cz.incad.kramerius.virtualcollections.CollectionsManager;

@Path("/v5.0/admin/vc")
public class VirtualCollectionsResource {

    public static final Logger LOGGER = Logger.getLogger(VirtualCollectionsResource.class.getName());

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    IsActionAllowed actionAllowed;

    @Inject
    Provider<User> userProvider;

    @Inject
    @Named("fedora")
    CollectionsManager manager;

    @POST
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response post(JSONObject jsonObj) {
        if (permit(this.userProvider.get())) {
            try {

                String createdPID = CollectionUtils.create(this.fedoraAccess);
                Collection vc = this.manager.getCollection(createdPID);
                if (vc != null) {
                    String label = jsonObj.has("label") ? jsonObj.getString("label") : "nolabel";
                    boolean canLeaveFlag = jsonObj.has("canLeave") ? jsonObj.getBoolean("canLeave") : false;
                    CollectionUtils.modify(createdPID, label, canLeaveFlag, fedoraAccess);
                    Collection newVc = this.manager.getCollection(createdPID);
                    if (newVc != null) {
                        if (jsonObj.has("descs")) {
                            Map<String, String> map = new HashMap<String, String>();
                            JSONObject descs = jsonObj.getJSONObject("descs");
                            for (Iterator keys = descs.keys(); keys.hasNext();) {
                                String k = (String) keys.next();
                                map.put(k.toString(), descs.getString(k.toString()));
                            }

                            CollectionUtils.modifyTexts(newVc.getPid(), fedoraAccess, map);
                            // new lookup
                            newVc = this.manager.getCollection(createdPID);
                        }
                    }
                    return Response.ok().entity(virtualCollectionTOJSON(newVc).toString()).build();
                } else {
                    throw new ObjectNotFound("cannot find virtual collection '" + createdPID + "'");
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new GenericApplicationException(e.getMessage());
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage());
            } catch (CollectionException e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else
            throw new ActionNotAllowed("action is not allowed");
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response put(JSONObject jsonObj) {
        if (permit(this.userProvider.get())) {
            try {
                String pid = jsonObj.getString("pid");
                Collection col = this.manager.getCollection(pid);
                if (col != null) {
                    String label = jsonObj.getString("label");
                    boolean canLeaveFlag = jsonObj.getBoolean("canLeave");
                    CollectionUtils.modify(pid, label, canLeaveFlag, fedoraAccess);
                    if (jsonObj.has("descs")) {
                        Map<String, String> map = new HashMap<String, String>();
                        JSONObject descs = jsonObj.getJSONObject("descs");

                        for (Iterator keys = descs.keys(); keys.hasNext();) {
                            String k = (String) keys.next();
                            map.put(k.toString(), descs.getString(k.toString()));
                        }

                        CollectionUtils.modifyTexts(pid, fedoraAccess, map);
                    }
                    JSONObject jsonObject = virtualCollectionTOJSON(this.manager.getCollection(pid));
                    return Response.ok().entity(jsonObject.toString()).build();
                } else {
                    throw new ObjectNotFound("cannot find virtual collection '" + pid + "'");
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new GenericApplicationException(e.getMessage());
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage());
            } catch (CollectionException e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else
            throw new ActionNotAllowed("action is not allowed");
    }

    @DELETE
    @Path("{pid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response delete(@PathParam("pid") String pid) {
        try {
            if (permit(this.userProvider.get())) {
                Collection vc = this.manager.getCollection(pid);
                if (vc != null) {
                    try {
                        try {
                            CollectionUtils.deleteWOIndexer(pid, fedoraAccess);
                        } catch (Exception e) {
                            throw new GenericApplicationException(e.getMessage());
                        }
                        JSONObject jsonObj = virtualCollectionTOJSON(vc);
                        jsonObj.put("deleted", true);
                        return Response.ok().entity(jsonObj.toString()).build();
                    } catch (JSONException e) {
                        throw new GenericApplicationException(e.getMessage());
                    }
                } else {
                    throw new ObjectNotFound("cannot find vc '" + pid + "'");
                }
            } else
                throw new ActionNotAllowed("action is not allowed");
        } catch (CollectionException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}")
    @Consumes
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response oneVirtualCollection(@PathParam("pid") String pid) {
        if (permit(this.userProvider.get())) {
            try {
                Collection col = this.manager.getCollection(pid);
                if (col != null) {
                    return Response.ok().entity(virtualCollectionTOJSON(col)).build();
                } else {
                    throw new ObjectNotFound("cannot find vc '" + pid + "'");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new GenericApplicationException(e.getMessage());
            }
        } else
            throw new ActionNotAllowed("action is not allowed");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response get() {
        if (permit(this.userProvider.get())) {
            try {
                List<Collection> collections = this.manager.getCollections();
                JSONArray jsonArr = new JSONArray();
                for (Collection c : collections) {
                    jsonArr.put(virtualCollectionTOJSON(c));
                }
                return Response.ok().entity(jsonArr.toString()).build();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new GenericApplicationException(e.getMessage());
            }
        } else
            throw new ActionNotAllowed("action is not allowed");
    }

    public static JSONObject virtualCollectionTOJSON(Collection vc) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pid", vc.getPid());
        jsonObj.put("label", vc.getLabel());
        jsonObj.put("canLeave", vc.isCanLeaveFlag());
        JSONObject jsonMap = new JSONObject();

        // Map map = new HashMap<String, String>();
        // for(CollectionDescription cd : descriptions){
        // map.put(cd.lang, cd.text);
        // }
        // return map;

        // Map<String, String> descMAp = vc.getDescriptionsMap();
        List<Description> descs = vc.getDescriptions();
        for (Description d : descs) {
            jsonMap.put(d.getLangCode(), d.getText());

        }

        jsonObj.put("descs", jsonMap);
        return jsonObj;
    }

    boolean permit(User user) {
        if (user != null)
            return this.actionAllowed.isActionAllowed(user, SecuredActions.VIRTUALCOLLECTION_MANAGE.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH);
        else
            return false;
    }

}
