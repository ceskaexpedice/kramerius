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
 * Ziskani nejnovejsich a neojblibenejsich
 * 
 * @author pavels
 * 
 */
public class FeederResourceClient {

    /**
     * Nejoblibenejsi
     * 
     * @return
     */
    public static String mostdesirable(String type, String limit, String offset) {
        Client c = Client.create();
        WebResource r = c
                .resource("http://localhost:8080/search/api/v5.0/feed/mostdesirable");
        r = params(type, limit, offset, r);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    /**
     * Nejnovejsi
     * 
     * @return
     */
    public static String newest(String type, String limit, String offset) {
        Client c = Client.create();
        WebResource r = c
                .resource("http://localhost:8080/search/api/v5.0/feed/newest");
        r = params(type, limit, offset, r);
        System.out.println(r.getURI());
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    public static WebResource params(String type, String limit, String offset,
            WebResource r) {
        if (type != null) {
            r = r.queryParam("type", type);
        }
        r = r.queryParam("limit", limit);
        r = r.queryParam("offset", offset);
        return r;
    }
}
