/*
 * Copyright (C) 2012 Pavel Stastny
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
package cz.incad.kramerius.rest.api.client.v46;

import java.util.Arrays;
import java.util.logging.Logger;

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
 * Simple testing utility - Jersey 3 version
 */
public class ProcessesClient {

    private static final String DEFAULT_NAME = "krameriusAdmin";
    private static final String DEFAULT_PSWD = "krameriusAdmin";

    private static final Logger LOGGER = Logger.getLogger(ProcessesClient.class.getName());

    private static Client createClient() {
        return ClientBuilder.newBuilder()
                .register(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD))
                .build();
    }

    public static String logs(String uuid) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v4.6/processes/" + uuid + "/logs");

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            if (response.getStatus() == 404) {
                LOGGER.severe("Process not found");
            }
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String stop(String uuid) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v4.6/processes/" + uuid + "?stop=true");

        try (Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.text(""))) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String planWithoutParams() {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v4.6/processes?def=mock");

        try (Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.text(""))) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String planProcWithParams() throws JSONException {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v4.6/processes?def=mock");

        JSONObject object = new JSONObject();
        object.put("parameters", new JSONArray(Arrays.asList("first", "second", "third")));

        try (Response response = target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(object.toString(), MediaType.APPLICATION_JSON))) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String planProcWithNamedParams() throws JSONException {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v4.6/processes?def=wmock");

        JSONObject object = new JSONObject();
        JSONObject mapping = new JSONObject();
        mapping.put("inputFolder", new JSONArray(new String[]{"/home/pavels/"}));
        mapping.put("processName", new JSONArray(new String[]{"Muj nazev procesu"}));
        object.put("mapping", mapping);

        try (Response response = target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(object.toString(), MediaType.APPLICATION_JSON))) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String desc(String uuid) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v4.6/processes/" + uuid);

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String delete(String uuid) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v4.6/processes/" + uuid);

        try (Response response = target.request(MediaType.APPLICATION_JSON).delete()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String list() {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v4.6/processes/");

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            if (response.getStatus() == 404) {
                LOGGER.severe("Process not found");
            }
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static void main(String[] args) throws JSONException, InterruptedException {
        String list = list();
        System.out.println(list);
    }

}