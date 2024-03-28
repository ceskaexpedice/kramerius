/*
 * Copyright (C) Mar 16, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.apiNew.client.v70;

import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.meta.Exclusive;
import javax.inject.Provider;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import com.google.inject.Inject;

import cz.incad.kramerius.rest.apiNew.client.v70.utils.RightRuntimeInformations;
import cz.incad.kramerius.rest.apiNew.client.v70.utils.RightRuntimeInformations.RuntimeInformation;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaLicenseUtils;
import cz.incad.kramerius.security.licenses.impl.lock.ExclusiveLockMapItemImpl;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLock;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMap;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMapItem;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMaps;

@Path("/client/v7.0/locks")
public class LocksResource extends ClientApiResource {
    
    public static final Logger LOGGER = Logger.getLogger(LocksResource.class.getName());
    
    @Inject
    ExclusiveLockMaps exclusiveLockMaps;

    @Inject
    Provider<User> userProvider;
    
    @GET
    @Path("{hash}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfo(@PathParam("hash") String hash) {
        try {
            User user = this.userProvider.get();
            ExclusiveLockMap lockMap = this.exclusiveLockMaps.findHash(hash);
            if (lockMap != null) {
                String tokenId = user.getSessionAttributes().get("token_id");
                synchronized (CriteriaLicenseUtils.INTERNAL_SYNC_LOCK) {
                    ExclusiveLockMapItem item = lockMap.findByTokenId(tokenId);
                    if (item != null) {
                        if (item.isValid(Instant.now())) {
                            JSONObject jsonObject = item.toJSONObject();
                            return Response.ok(jsonObject.toString()).type(MediaType.APPLICATION_JSON).build();
                        } else {
                            LOGGER.log(Level.SEVERE,"Item is not valid!");
                            lockMap.deregisterItem(item);
                            return Response.status(Status.NOT_FOUND).build();
                        }
                    } else {
                        LOGGER.log(Level.SEVERE,"Item by token id is not found");
                        return Response.status(Status.NOT_FOUND).build();
                    }
                }
            } else {
                LOGGER.log(Level.SEVERE,"Exclusive map not found ");
                return Response.status(Status.NOT_FOUND).build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
    

    //consider put
    @GET
    @Path("{hash}/refresh")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response refresh(@PathParam("hash") String hash) {
        try {
            User user = this.userProvider.get();
            ExclusiveLockMap lockMap = this.exclusiveLockMaps.findHash(hash);
            if (lockMap != null) {
                String tokenId = user.getSessionAttributes().get("token_id");
                if (tokenId != null) {
                    synchronized (CriteriaLicenseUtils.INTERNAL_SYNC_LOCK) {
                        ExclusiveLockMapItem item = lockMap.findByTokenId(tokenId);
                        if (item != null && item.isValid(Instant.now())) {
                            item.refresh(Instant.now());
                            JSONObject jsonObject = item.toJSONObject();
                            return Response.ok(jsonObject.toString()).type(MediaType.APPLICATION_JSON).build();
                        } else {
                            if (lockMap.checkAvailabitlity()) {
                                ExclusiveLock lock = lockMap.getAssociatedExcelusiveLock();
                                Instant now = Instant.now();
                                ExclusiveLockMapItem nitem = new ExclusiveLockMapItemImpl(tokenId, lock.getRefreshInterval(), now, now, now.plusSeconds(lock.getMaxInterval()), user.getLoginname());
                                lockMap.registerItem(nitem);
                                return Response.ok(nitem.toJSONObject().toString()).type(MediaType.APPLICATION_JSON).build();
                            } else {
                                return Response.status(429).type(MediaType.APPLICATION_JSON).build();
                            }
                        }
                    }
                } else {
                    LOGGER.log(Level.SEVERE,"Token id not found!");
                    return Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build();
                }
            } else {
                LOGGER.log(Level.SEVERE,"Hash table not found!");
                return Response.status(Status.NOT_FOUND).build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


    @DELETE
    @Path("{hash}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response delete(@PathParam("hash") String hash) {
        try {
            User user = this.userProvider.get();
            ExclusiveLockMap lockMap = this.exclusiveLockMaps.findHash(hash);
            String tokenId = user.getSessionAttributes().get("token_id");
            synchronized (CriteriaLicenseUtils.INTERNAL_SYNC_LOCK) {
                ExclusiveLockMapItem item = lockMap.findByTokenId(tokenId);
                if (item != null) {
                    lockMap.deregisterItem(item);
                    return Response.ok().type(MediaType.APPLICATION_JSON).build();
                } else {
                    return Response.status(Status.NOT_FOUND).build();
                }
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

}
