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
package cz.incad.kramerius.processes.client;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * PluginEndpoint
 * @author petrp
 */
@Path("/plugin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcessManagerPluginEndpoint {

    @GET
    @Path("/{pluginId}")
    public Response getPlugin(@PathParam("pluginId") String pluginId) {
        String profile = "{" +
                "  \"pluginId\": \"" + pluginId + "\"," +
                "  \"description\" : null," +
                "  \"mainClass\" : null," +
                "  \"payloadFieldSpecMap\" : null," +
                "  \"scheduledProfiles\" : [ \"testPlugin2-small\", \"testPlugin3-big\" ]," +
                "  \"profiles\" : null" +
                "}";
        return jsonPayload(profile);
    }

    private static Response jsonPayload(String jsonPayload) {
        return Response.ok(jsonPayload, MediaType.APPLICATION_JSON).build();
    }

}
