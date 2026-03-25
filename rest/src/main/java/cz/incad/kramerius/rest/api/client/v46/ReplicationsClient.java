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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;

/**
 * Simple testing utility - Jersey 3 / Jakarta
 */
public class ReplicationsClient {

    private static final String DEFAULT_NAME = "krameriusAdmin";
    private static final String DEFAULT_PSWD = "kram";

    private static Client createClient() {
        return ClientBuilder.newBuilder()
                .register(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD))
                .build();
    }

    public static String tree(String uuid) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v4.6/replication/" + uuid + "/tree");

        try (Response response = target.request().get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String foxmlAsXML(String uuid) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v4.6/replication/" + uuid + "/foxml");

        try (Response response = target.request(MediaType.APPLICATION_XML).get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String foxmlAsJSON(String uuid) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v4.6/replication/" + uuid + "/foxml");

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    public static String desc(String uuid) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v4.6/replication/" + uuid);

        try (Response response = target.request().get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }
}