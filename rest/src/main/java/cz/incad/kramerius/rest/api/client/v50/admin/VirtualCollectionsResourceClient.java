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
package cz.incad.kramerius.rest.api.client.v50.admin;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;

/**
 * Virtual collections management - Jersey 3 / Jakarta
 */
public class VirtualCollectionsResourceClient {

    private static final String DEFAULT_NAME = "krameriusAdmin";
    private static final String DEFAULT_PSWD = "krameriusAdmin";

    private static Client createClient() {
        return ClientBuilder.newBuilder()
                .register(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD))
                .build();
    }

    /**
     * Create a new virtual collection
     */
    public static String createVirtualCollection() throws JSONException {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/vc");

        JSONObject object = new JSONObject();
        object.put("label", "nejnovejsi");
        object.put("canLeave", true);

        JSONObject descs = new JSONObject();
        descs.put("cs", "Pokus o neco");
        descs.put("en", "attempt to something");
        object.put("descs", descs);

        try (Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(object.toString(), MediaType.APPLICATION_JSON))) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    /**
     * Delete an existing virtual collection
     */
    public static String deleteVirtualCollection(String vc) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/vc/" + vc);

        try (Response response = target.request(MediaType.APPLICATION_JSON)
                .delete()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }
}