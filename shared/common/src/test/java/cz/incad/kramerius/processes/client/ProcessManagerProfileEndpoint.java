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
 * ProfileEndpoint
 * @author petrp
 */
@Path("/profile")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcessManagerProfileEndpoint {

    @GET
    @Path("/{profileId}")
    public Response getProfile(@PathParam("profileId") String profileId) {
        String profile = "{" +
                "  \"profileId\": \"" + profileId + "\"," +
                "  \"description\" : \"testPlugin1-big-description\"," +
                "  \"pluginId\" : \"testPlugin1\"," +
                "  \"jvmArgs\" : null" +
                "}";
        return jsonPayload(profile);
    }

    @GET
    public Response getProfiles() {
        String profiles = "[" +
                "    {" +
                "        \"profileId\": \"import\"," +
                "            \"description\": \"Import\"," +
                "            \"pluginId\": \"import\"," +
                "            \"jvmArgs\": [" +
                "        \"-Xms1g\"," +
                "                \"-Xmx16g\"," +
                "                \"-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=58001\"" +
                "    ]" +
                "    }," +
                "    {" +
                "        \"profileId\": \"testPlugin1-big\"," +
                "            \"description\": \"Big profile\"," +
                "            \"pluginId\": \"testPlugin1\"," +
                "            \"jvmArgs\": [" +
                "        \"-Xms1g\"," +
                "                \"-Xmx32g\"" +
                "    ]" +
                "    }," +
                "    {" +
                "        \"profileId\": \"testPlugin1-small\"," +
                "            \"description\": \"Small profile\"," +
                "            \"pluginId\": \"testPlugin1\"," +
                "            \"jvmArgs\": [" +
                "        \"-Xms1g\"," +
                "                \"-Xmx4g\"" +
                "    ]" +
                "    }," +
                "    {" +
                "        \"profileId\": \"testPlugin2\"," +
                "            \"description\": \"testPlugin2\"," +
                "            \"pluginId\": \"testPlugin2\"," +
                "            \"jvmArgs\": []" +
                "    }" +
                "]";
        return jsonPayload(profiles);
    }

    @PUT
    @Path("/{profileId}")
    public Response updateProfile(@PathParam("profileId") String profileId, String profile) {
        return jsonPayload(profile);
    }

    private static Response jsonPayload(String jsonPayload) {
        return Response.ok(jsonPayload, MediaType.APPLICATION_JSON).build();
    }
}
