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
package cz.incad.kramerius.rest.api.k5.client.rights;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.virtualcollections.CollectionException;
import cz.incad.kramerius.virtualcollections.CollectionsManager;

@Path("/v5.0/rights")
public class ClientRightsResource {

    public static final Logger LOGGER = Logger.getLogger(ClientRightsResource.class.getName());

    @Inject
    IsActionAllowed actionAllowed;

    @Inject
    SolrAccess solrAccess;
    
    @Inject
    @Named("solr")
    CollectionsManager colGet;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response allowedActions(@QueryParam("actions") String actionNames, @QueryParam("pid") String pid,
            @QueryParam("stream") String stream, @QueryParam("fullpath") boolean fullp) {
        try {
            if (pid == null)
                pid = SpecialObjects.REPOSITORY.getPid();
            ObjectPidsPath[] paths = this.solrAccess.getPath(pid);

            if (actionNames == null) {
                SecuredActions[] vls = SecuredActions.values();
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < vls.length; i++) {
                    if (i > 0)
                        builder.append(',');
                    builder.append(vls[i].getFormalName());
                }
                actionNames = builder.toString();
            }

            JSONObject object = new JSONObject();
            if (fullp) {
                fullPath(actionNames, pid, stream, paths, object);
            } else {
                onePath(actionNames, pid, stream, paths, object);
            }
            return Response.ok().entity(object.toString()).build();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        } catch (CollectionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }
    }

    private void fullPath(String actionNames, String pid, String stream, ObjectPidsPath[] paths, JSONObject object)
            throws JSONException, CollectionException {

        StringTokenizer tokenizer = new StringTokenizer(actionNames, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            object.put(token, new JSONArray());
            for (ObjectPidsPath ph : paths) {
                ObjectPidsPath nph = ph.injectRepository().injectCollections(this.colGet);
                boolean[] flags = this.actionAllowed.isActionAllowedForAllPath(token, pid, stream, nph);
                allowedFor(object.getJSONArray(token), token, nph, flags);
            }
        }

    }

    private void onePath(String actionNames, String pid, String stream, ObjectPidsPath[] paths, JSONObject object)
            throws JSONException {
        StringTokenizer tokenizer = new StringTokenizer(actionNames, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            boolean flag = false;
            for (ObjectPidsPath ph : paths) {
                flag = this.actionAllowed.isActionAllowed(token, pid, stream, ph);
                if (flag)
                    break;
            }
            allowedFor(object, token, flag);
        }
    }

    private JSONArray allowedFor(JSONArray jsonArr, String action, ObjectPidsPath path, boolean[] flags)
            throws JSONException {
        JSONObject pathSon = new JSONObject();

        String[] fromRootToLeaf = path.getPathFromRootToLeaf();
        for (int i = 0; i < fromRootToLeaf.length; i++) {
            pathSon.put(fromRootToLeaf[i], flags[i]);
        }
        jsonArr.put(pathSon);

        return jsonArr;
    }

    private JSONObject allowedFor(JSONObject jsonObj, String action, boolean flag) throws JSONException {
        jsonObj.put(action, flag);
        return jsonObj;
    }

}
