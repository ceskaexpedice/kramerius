/*
 * Copyright (C) Mar 17, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.apiNew.admin.v70;

import java.util.List;
import java.util.logging.Level;

import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;

import com.google.inject.Inject;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.lock.ExclusiveReadersLock.ExclusiveLockType;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMap;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMaps;

@Path("/admin/v7.0/locks")
public class AdminLockResource extends AdminApiResource {

    @Inject
    ExclusiveLockMaps exclusiveLockMaps;

    @Inject
    Provider<User> userProvider;

    @Inject
    LicensesManager licensesManager;

    
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getLocks() {
        if (permit(this.userProvider.get()))  {
            try {
                JSONArray jsonArray = new JSONArray();
                List<String> allHashes = this.exclusiveLockMaps.getAllHashes();
                for (String h : allHashes) {
                    ExclusiveLockMap lockMap = this.exclusiveLockMaps.findHash(h);
                    jsonArray.put(lockMap.toJSONHeaderObject());
                }
                return Response.ok(jsonArray.toString()).build();
            } catch (WebApplicationException e) {
                throw e;
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new InternalErrorException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("action is not allowed");
        }
    }
    

    
    @GET
    @Path("license/{license}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getLocksByLicense(@PathParam("license") String license) {
        if (permit(this.userProvider.get()))  {
            try {
                License lic = this.licensesManager.getLicenseByName(license);
                if (lic != null && lic.exclusiveLockPresent()) {
                    JSONArray jsonArray = new JSONArray();
                    List<String> allHashes = this.exclusiveLockMaps.getAllHashes();
                    for (String h : allHashes) {
                        ExclusiveLockMap lockMap = this.exclusiveLockMaps.findHash(h);
                        if (lockMap.getAssociatedLicense().getName().equals(license)) {
                            ExclusiveLockType lockType = lockMap.getLockType();
                            if (lic.getExclusiveLock().getType().equals(lockType)) {
                                jsonArray.put(lockMap.toJSONHeaderObject());
                            }
                        }
                    }
                    return Response.ok(jsonArray.toString()).build();
                } else {
                    throw new NotFoundException();
                }
            } catch (WebApplicationException e) {
                throw e;
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new InternalErrorException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("action is not allowed");
        }
    }
    
    @GET
    @Path("{hash}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getLock(@PathParam("hash") String hash) {
        if (permit(this.userProvider.get()))  {
            try {
                ExclusiveLockMap lock = this.exclusiveLockMaps.findHash(hash);
                if (lock != null) {
                    return Response.ok(lock.toJSONObject()).build();
                } else {
                    return Response.status(Status.NOT_FOUND).build();
                }
                
            } catch (WebApplicationException e) {
                throw e;
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new InternalErrorException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("action is not allowed");
        }
    }

    
    boolean permit(User user) {
        if (user != null)
            return  this.rightsResolver.isActionAllowed(user, SecuredActions.A_RIGHTS_EDIT.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH).flag();
        else
            return false;
    }

}
