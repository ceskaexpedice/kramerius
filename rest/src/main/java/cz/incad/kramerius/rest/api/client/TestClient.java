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
package cz.incad.kramerius.rest.api.client;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;

public class TestClient {

    public static String getJSON(String url) throws IOException {
    	Client c = Client.create();
    	c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        WebResource r = c.resource(url);
    	Builder builder = r.accept(MediaType.APPLICATION_JSON);
		return builder.get(String.class);
    }

    public static String getXML(String url) throws IOException {
    	Client c = Client.create();
    	c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        WebResource r = c.resource(url);
    	Builder builder = r.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML);
    	return builder.get(String.class);
    }

        public static void main(String[] args) throws IOException {
    		String q = "http://localhost:8080/search/api/v5.0/search?q=*:*&facet=true&rows=0&facet.field=fedora.model&facet.field=keywords&facet.field=collection";
    		String xml = getXML(q);
    		System.out.println(xml);
    		String json = getJSON(q);
    		System.out.println(json);
    		
    	}
}
