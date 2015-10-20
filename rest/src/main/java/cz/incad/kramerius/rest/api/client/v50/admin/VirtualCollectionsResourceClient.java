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

import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.BasicAuthenticationFilter;

/**
 * Virtualni sbirky 
 * @author pavels
 */
public class VirtualCollectionsResourceClient {

    private static final String DEFAULT_NAME = "krameriusAdmin";
	private static final String DEFAULT_PSWD = "krameriusAdmin";
	
	/**
	 * Vytvoreni nove
	 * @return
	 * @throws JSONException 
	 */
	public static String createVirtualCollection() throws JSONException {
    	Client c = Client.create();

        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/vc");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        JSONObject object = new JSONObject();

        object.put("label", "nejnovejsi");
        object.put("canLeave", true);
        
        JSONObject descs = new JSONObject();
        descs.put("cs", "Pokus o neco");
        descs.put("en", "attempt to something");
        object.put("descs", descs);
        
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(object.toString(), MediaType.APPLICATION_JSON).post(String.class);
        return t;
    }
	
	/**
	 * Smazani stare
	 * @param vc
	 * @return
	 */
	public static String deleteVirtualCollection(String vc) {
    	Client c = Client.create();

        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/vc/"+vc);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));

        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).delete(String.class);
        return t;
	}


}
