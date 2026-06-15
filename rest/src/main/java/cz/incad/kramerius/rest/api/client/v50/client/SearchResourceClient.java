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
 * Ziskava informace z indexu
 * @author pavels
 */
public class SearchResourceClient {

    private static final String BASE_URL = "http://localhost:8080/search/api/v5.0/search";

    /**
     * Vyhledavani v SOLRu, JSON format
     */
    public static String search(String query) {
        try (Client client = ClientBuilder.newBuilder().build()) {
            WebTarget target = client.target(BASE_URL + "?" + query);
            try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
                return response.readEntity(String.class);
            }
        }
    }

    /**
     * Vyhledavani v SOLRu, XML format
     */
    public static String searchXML(String query) {
        try (Client client = ClientBuilder.newBuilder().build()) {
            WebTarget target = client.target(BASE_URL + "?" + query);
            try (Response response = target.request(MediaType.APPLICATION_XML).get()) {
                return response.readEntity(String.class);
            }
        }
    }

    /**
     * Komponenta terms
     */
    public static String terms(String query) {
        try (Client client = ClientBuilder.newBuilder().build()) {
            WebTarget target = client.target(BASE_URL + "/terms?" + query);
            try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
                return response.readEntity(String.class);
            }
        }
    }

    public static void main(String[] args) {
        String all = search("q=*:*");
        System.out.println(all);

        all = searchXML("q=*:*");
        System.out.println(all);

        // Example of using terms component
        String terms = terms("terms.fl=language");
        System.out.println(terms);
    }
}