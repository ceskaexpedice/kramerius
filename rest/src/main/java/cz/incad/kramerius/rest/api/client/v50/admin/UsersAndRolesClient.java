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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;

/**
 * User and role administration - Jersey 3 / Jakarta
 */
public class UsersAndRolesClient {

    private static final String DEFAULT_NAME = "krameriusAdmin";
    private static final String DEFAULT_PSWD = "krameriusAdmin";

    private static Client createClient() {
        return ClientBuilder.newBuilder()
                .register(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD))
                .build();
    }

    public static String deleteUser(String userId) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/users/" + userId);

        try (Response response = target.request(MediaType.APPLICATION_JSON).delete()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String createUser() throws JSONException {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/users");

        JSONObject object = new JSONObject();
        object.put("lname", "krakonos");
        object.put("firstname", "Created from client");
        object.put("surname", "Created from client");
        object.put("password", "jelito");

        try (Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(object.toString(), MediaType.APPLICATION_JSON))) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String createRole() {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/roles");

        JSONObject object = new JSONObject();
        object.put("id", -1);
        object.put("name", "moje_role");

        try (Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(object.toString(), MediaType.APPLICATION_JSON))) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String users() {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/users");

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String roles() {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/roles");

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String role(int rid) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/roles/" + rid);

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String user(int uid) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/users/" + uid);

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String userChange(int uid, JSONObject userObject) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/admin/users/" + uid);

        try (Response response = target.request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(userObject.toString(), MediaType.APPLICATION_JSON))) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String appendRole(int uid, int roleid) {
        String roleString = role(roleid);
        String userString = user(uid);

        JSONObject roleObj = new JSONObject(roleString);
        JSONObject jsonObj = new JSONObject(userString);
        JSONArray jsonArray = jsonObj.getJSONArray("roles");
        jsonArray.put(roleObj);

        return userChange(uid, jsonObj);
    }
}