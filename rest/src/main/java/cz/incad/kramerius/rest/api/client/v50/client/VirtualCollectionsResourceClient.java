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
 * Informace o virtualnich sbirkach
 * @author pavels
 */
public class VirtualCollectionsResourceClient {

    private static final String BASE_URL = "http://localhost:8080/search/api/v5.0";

    /**
     * Seznam vsech virtualnich sbirek
     */
    public static String vcs() {
        try (Client client = ClientBuilder.newBuilder().build()) {
            WebTarget target = client.target(BASE_URL + "/vc");
            try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
                return response.readEntity(String.class);
            }
        }
    }

    /**
     * Konkretni virtualni sbirka
     */
    public static String vc(String vcpid) {
        try (Client client = ClientBuilder.newBuilder().build()) {
            WebTarget target = client.target(BASE_URL + "/vc/" + vcpid);
            try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
                return response.readEntity(String.class);
            }
        }
    }

    /**
     * Info o objektu
     */
    public static String vcAsFedoraObject(String vcpid) {
        try (Client client = ClientBuilder.newBuilder().build()) {
            WebTarget target = client.target(BASE_URL + "/item/" + vcpid);
            try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
                return response.readEntity(String.class);
            }
        }
    }

    /**
     * Seznam vsech streamu objektu reprezentujici virtualni sbirku
     */
    public static String vcAsStreamsObject(String vcpid) {
        try (Client client = ClientBuilder.newBuilder().build()) {
            WebTarget target = client.target(BASE_URL + "/item/" + vcpid + "/streams");
            try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
                return response.readEntity(String.class);
            }
        }
    }

    /**
     * Ziskani streamu z objektu virtualni sbirky
     */
    public static String vcAsStreamObject(String vcpid, String str) {
        try (Client client = ClientBuilder.newBuilder().build()) {
            WebTarget target = client.target(BASE_URL + "/item/" + vcpid + "/streams/" + str);
            try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
                return response.readEntity(String.class);
            }
        }
    }

    public static void main(String[] args) {
        String vcs = vcs();
        System.out.println(vcs);

        String vc = vc("vc:f73dee31-ae76-4dbc-b7b9-d986df497596");
        System.out.println(vc);

        String vcFedora = vcAsFedoraObject("vc:f73dee31-ae76-4dbc-b7b9-d986df497596");
        System.out.println(vcFedora);

        String vcStreams = vcAsStreamsObject("vc:f73dee31-ae76-4dbc-b7b9-d986df497596");
        System.out.println(vcStreams);

        String vcDCStream = vcAsStreamObject("vc:f73dee31-ae76-4dbc-b7b9-d986df497596", "DC");
        System.out.println(vcDCStream);
    }
}