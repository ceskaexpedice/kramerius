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

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * Ziskava informace z indexu
 * 
 * @author pavels
 * 
 */
public class SearchResourceClient {

    /**
     * Vyhledavani v SOLRu
     * 
     * @param query
     * @return
     */
    public static String search(String query) {
        Client c = Client.create();
        WebResource r = c
                .resource("http://localhost:8080/search/api/v5.0/search?"
                        + query);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    public static String searchXML(String query) {
        Client c = Client.create();
        WebResource r = c
                .resource("http://localhost:8080/search/api/v5.0/search?"
                        + query);
        String t = r.accept(MediaType.APPLICATION_XML).get(String.class);
        return t;
    }

    /**
     * Komponenta terms
     * 
     * @param query
     * @return
     */
    public static String terms(String query) {
        Client c = Client.create();
        WebResource r = c
                .resource("http://localhost:8080/search/api/v5.0/search/terms?"
                        + query);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    public static void main(String[] args) {
        String all = search("q=*:*");
        System.out.println(all);

        all = searchXML("q=*:*");
        System.out.println(all);

//        String monographs = search("q=fedora.model:monograph");
//        System.out.println(monographs);
//
//        String terms = terms("terms.fl=language");
//        System.out.println(terms);
    }

}
