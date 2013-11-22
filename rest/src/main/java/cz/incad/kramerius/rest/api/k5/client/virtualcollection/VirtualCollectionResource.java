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
package cz.incad.kramerius.rest.api.k5.client.virtualcollection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.virtualcollections.VirtualCollection;
import cz.incad.kramerius.virtualcollections.VirtualCollection.CollectionDescription;
import cz.incad.kramerius.virtualcollections.VirtualCollectionsManager;
import javax.ws.rs.PathParam;

@Path("/k5/vc")
public class VirtualCollectionResource {

    public static final Logger LOGGER = Logger.getLogger(VirtualCollectionResource.class.getName());

    @Inject
    VirtualCollectionsManager manager;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @GET
    @Path("{language}")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response list(@PathParam("language")String language) {
        try {
            ArrayList<String> langs = new ArrayList<String>();
            langs.add(language);
            JSONArray jsonArray = new JSONArray();
            List<VirtualCollection> vcs = manager.getVirtualCollections(this.fedoraAccess, langs);
            for (VirtualCollection vc : vcs) {
                JSONObject jsonObj = new JSONObject();
                String label = vc.getLabel();
                jsonObj.put("label", vc.getLabel());
                jsonObj.put("pid", vc.getPid());

                List<CollectionDescription> descs = vc.getDescriptions();
                if (!descs.isEmpty()) {
                    //JSONArray descArr = new JSONArray();
                    for (CollectionDescription cdesc : descs) {
                        jsonObj.put(cdesc.getLang(), cdesc.getText());
                    }
                }
                jsonArray.add(jsonObj);

            }
            return Response.ok().entity(jsonArray.toString()).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.ok().entity("{}").build();
        }

    }
    
    @GET
    @Path("{pid}/{language}")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response select(@PathParam("pid")String pid, @PathParam("language")String language) {
        try {
            ArrayList<String> langs = new ArrayList<String>();
            langs.add(language);
            JSONArray jsonArray = new JSONArray();
            VirtualCollection vc = manager.getVirtualCollection(this.fedoraAccess, pid, langs);
            
                JSONObject jsonObj = new JSONObject();
                String label = vc.getLabel();
                jsonObj.put("label", vc.getLabel());
                jsonObj.put("pid", vc.getPid());

                List<CollectionDescription> descs = vc.getDescriptions();
                if (!descs.isEmpty()) {
                    //JSONArray descArr = new JSONArray();
                    for (CollectionDescription cdesc : descs) {
                        jsonObj.put(cdesc.getLang(), cdesc.getText());
                    }
                }
                jsonArray.add(jsonObj);

            
            return Response.ok().entity(jsonArray.toString()).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.ok().entity("{}").build();
        }

    }

}
