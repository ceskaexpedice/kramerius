/*
 * Copyright (C) 2025 Inovatika
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


import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.workmode.WorkModeService;

import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * WorkModeResource
 * @author ppodsednik
 */
@Path("/admin/v7.0/workmode")
public class WorkModeResource  extends AdminApiResource{

    public static final Logger LOGGER = Logger.getLogger(WorkModeResource.class.getName());

    @javax.inject.Inject
    @Named("dbWorkMode")
    WorkModeService workModeService;

    @javax.inject.Inject
    Provider<User> userProvider;

    @javax.inject.Inject
    RightsResolver rightsResolver;


    /**
     * client receives: { "readOnly": true }
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getWorkModeStatus() {
        try {
            boolean readOnly = workModeService.isReadOnlyMode();
            return Response.ok(new WorkModeStatusResponse(readOnly)).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * json from client: { "readOnly": true }
     * @param request
     * @return
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateWorkMode(WorkModeUpdateRequest request) {
        try {
            workModeService.setReadOnlyMode(request.readOnly);
            return Response.ok(new WorkModeStatusResponse(request.readOnly)).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    // DTO for request body
    public static class WorkModeUpdateRequest {
        public boolean readOnly;

        public WorkModeUpdateRequest() {}
        public WorkModeUpdateRequest(boolean readOnly) {
            this.readOnly = readOnly;
        }
    }

    // DTO for response
    public static class WorkModeStatusResponse {
        public boolean readOnly;

        public WorkModeStatusResponse() {}
        public WorkModeStatusResponse(boolean readOnly) {
            this.readOnly = readOnly;
        }
    }

}