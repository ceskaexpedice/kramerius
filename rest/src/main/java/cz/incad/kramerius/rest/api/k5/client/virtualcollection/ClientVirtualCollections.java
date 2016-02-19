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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.admin.vc.VirtualCollectionsResource;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;

import cz.incad.kramerius.security.utils.PasswordDigest;
import cz.incad.kramerius.virtualcollections.CDKVirtualCollectionsGet;
import cz.incad.kramerius.virtualcollections.VirtualCollection;
import cz.incad.kramerius.virtualcollections.VirtualCollection.CollectionDescription;
import cz.incad.kramerius.virtualcollections.impl.CDKResourcesFilter;

import cz.incad.kramerius.virtualcollections.VirtualCollection;

import cz.incad.kramerius.virtualcollections.VirtualCollectionsManager;

@Path("/v5.0/vc")
public class ClientVirtualCollections {

    public static final Logger LOGGER = Logger
            .getLogger(ClientVirtualCollections.class.getName());

    @Inject
    VirtualCollectionsManager manager;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    CDKVirtualCollectionsGet cdkVirtGet;
    

    
    @GET
    @Path("{pid}")
    @Consumes
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response oneVirtualCollection(@PathParam("pid") String pid) {
        try {
            String res = this.cdkVirtGet.getResource(pid);
            if (res == null) {
                List<VirtualCollection> vcs = this.cdkVirtGet.virtualCollections();
                for (VirtualCollection vc : vcs) {
                    if (vc.getPid().equals(pid)) {
                        return Response
                                .ok()
                                .entity(VirtualCollectionsResource
                                        .virtualCollectionTOJSON(vc)).build();
                        
                    }
                }
                throw new ObjectNotFound("cannot find vc '" + pid + "'");
            } else {
                VirtualCollection vc = this.cdkVirtGet.virtualCollectionsFromResource(pid, res);
                return Response
                        .ok()
                        .entity(VirtualCollectionsResource
                                .virtualCollectionTOJSON(vc)).build();
                
            }
        } catch (ObjectNotFound e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response get() {
        try {
            List<VirtualCollection> vcs = this.cdkVirtGet.virtualCollections();
            JSONArray jsonArr = new JSONArray();
            for (VirtualCollection vc : vcs) {
                jsonArr.put(VirtualCollectionsResource
                        .virtualCollectionTOJSON(vc));
            }
            return Response.ok().entity(jsonArr.toString()).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }
    }

}
