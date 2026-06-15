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
 * Manipulation with rights - Jersey 3 version
 */
public class RightsClient {

    public static final String DEFAULT_NAME = "krameriusAdmin";
    public static final String DEFAULT_PSWD = "krameriusAdmin";

    private static Client createClient() {
        return ClientBuilder.newBuilder()
                .register(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD))
                .build();
    }

    public static String deleteRight(String delId) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/rights/" + delId);

        try (Response response = target.request(MediaType.APPLICATION_JSON).delete()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String createRight(JSONObject jsonObj) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/rights");

        try (Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(jsonObj.toString(), MediaType.APPLICATION_JSON))) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String rights() {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/rights");

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String right(String id) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/rights/" + id);

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String params() {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/rights/params");

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String param(String paramId) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/rights/params/" + paramId);

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String createParam(JSONObject json) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/rights/params");

        try (Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(json.toString(), MediaType.APPLICATION_JSON))) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String deleteParam(String paramId) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/rights/params/" + paramId);

        try (Response response = target.request(MediaType.APPLICATION_JSON).delete()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    private static String createSampleRight() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("action", "read");
        jsonObj.put("pid", "uuid:1");
        jsonObj.put("role", new JSONObject(UsersAndRolesClient.role(3)));

        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/rights");

        try (Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(jsonObj.toString(), MediaType.APPLICATION_JSON))) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    private static String createSampleRight2(String critqname, JSONObject param) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("action", "read");
        jsonObj.put("pid", "uuid:1");
        jsonObj.put("role", new JSONObject(UsersAndRolesClient.role(3)));

        JSONObject critJSON = new JSONObject();
        critJSON.put("qname", critqname);
        critJSON.put("params", param);

        jsonObj.put("criterium", critJSON);

        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/rights");

        try (Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(jsonObj.toString(), MediaType.APPLICATION_JSON))) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }
}