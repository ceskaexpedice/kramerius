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
package cz.incad.kramerius.rest.api.client.v50.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Access newest and most desirable feeds - Jersey 3 / Jakarta
 *
 * Author: pavels
 */
public class FeederResourceClient {

    private static Client createClient() {
        return ClientBuilder.newClient();
    }

    /**
     * Most desirable items
     */
    public static String mostdesirable(String type, String limit, String offset) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/feed/mostdesirable");
        target = applyParams(target, type, limit, offset);

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    /**
     * Newest items
     */
    public static String newest(String type, String limit, String offset) {
        Client client = createClient();
        WebTarget target = client.target("http://localhost:8080/search/api/v5.0/feed/newest");
        target = applyParams(target, type, limit, offset);

        System.out.println(target.getUri());

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return response.readEntity(String.class);
        } finally {
            client.close();
        }
    }

    private static WebTarget applyParams(WebTarget target, String type, String limit, String offset) {
        if (type != null) {
            target = target.queryParam("type", type);
        }
        if (limit != null) {
            target = target.queryParam("limit", limit);
        }
        if (offset != null) {
            target = target.queryParam("offset", offset);
        }
        return target;
    }
}