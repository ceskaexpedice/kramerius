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

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.BasicAuthenticationFilter;

/**
 * Simple testing utility 
 * @author pavels
 */
public class ReplicationsClient {

    private static final String DEFAULT_NAME = "krameriusAdmin";
    private static final String DEFAULT_PSWD = "kram";
    
    
    /**
     * List of object designated to replication
     * @param uuid Master uuid 
     * @return
     */
    public static String tree(String uuid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/replication/"+uuid+"/tree");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.get(String.class);
        return t;
    }

    /**
     * Returns foxml data as pure xml
     * @param uuid
     * @return
     */
    public static String foxmlAsXML(String uuid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/replication/"+uuid+"/foxml");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_XML).get(String.class);
        return t;
    }

    /**
     * Returns foxml data wrapped in json object
     * @param uuid
     * @return
     */
    public static String foxmlAsJSON(String uuid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/replication/"+uuid+"/foxml");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    /**
     * Returns json description
     * @param uuid
     * @return
     */
    public static String desc(String uuid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/replication/"+uuid);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.get(String.class);
        return t;
    }

}
