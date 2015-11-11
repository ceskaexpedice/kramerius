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

import cz.incad.kramerius.rest.api.client.v46.ProcessesClient;
import cz.incad.kramerius.utils.BasicAuthenticationFilter;

/**
 * Administrace uzivatelu a roli
 * @author pavels
 *
 */
public class UsersAndRolesClient {

	private static final String DEFAULT_NAME = "krameriusAdmin";
	private static final String DEFAULT_PSWD = "krameriusAdmin";

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProcessesClient.class.getName());
    
    /**
     * Smaze uzivatele
     * @param userId
     * @return
     */
    public static String deleteUser(String userId) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/users/"+userId);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).delete(String.class);
        return t;
    }

    /**
     * Vytvoreni uzivatele
     * @return
     * @throws JSONException 
     */
    public static String createUser() throws JSONException {
    	Client c = Client.create();

        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/users");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        JSONObject object = new JSONObject();

        object.put("lname", "krakonos");
        object.put("firstname", "Created from client");
        object.put("surname", "Created from client");
        object.put("password","jelito-nenimilito");
        
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(object.toString(), MediaType.APPLICATION_JSON).post(String.class);
        return t;
    }
    
    /**
     * Seznam uzivatelu
     * @return
     */
    public static String users() {
    	Client c = Client.create();

        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/users");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
    	
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    /**
     * Seznam roli
     * @return
     */
    public static String roles() {
    	Client c = Client.create();

        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/roles");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
    	
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    /**
     * Konkretni role
     * @param rid
     * @return
     */
    public static String role(String rid) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/roles/"+rid);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }
    
    
    public static void main(String[] args) {
		String roles = roles();
		System.out.println(roles);
	}
}
