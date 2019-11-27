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
 * Manipulace s pravy
 * @author pavels
 *
 */
public class RightsClient {
	
	public static final String DEFAULT_NAME="krameriusAdmin";
	public static final String DEFAULT_PSWD="krameriusAdmin";
	
	/**
	 * Smazani prava
	 * @param delId
	 * @return
	 */
    public static String deleteRight(String delId) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/rights/"+delId);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).delete(String.class);
        return t;
    }
    
    /**
     * Vytvoreni prava
     * @param jsonObj
     * @return
     */
    public static String createRight(JSONObject jsonObj) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/rights");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(jsonObj.toString()).post(String.class);
        return t;
    }
    
    /**
     * Vypis vsech prav
     * @return
     */
    public static String rights() {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/rights");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    /**
     * Jedno pravo
     * @param id
     * @return
     */
    public static String right(String id) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/rights/"+id);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    /**
     * Vypis vsech parametru
     * @return
     */
    public static String params() {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/rights/params");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    /**
     * Jeden parameter
     * @param paramId
     * @return
     */
    public static String param(String paramId) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/rights/params/"+paramId);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    /**
     * Vytvoreni jednoho parametru
     * @param json
     * @return
     */
    public static String createParam(JSONObject json) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/rights/params");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(json.toString()).post(String.class);
        return t;
    }

    /**
     * Smazani parametru
     * @param paramId
     * @return
     */
    public static String deleteParam(String paramId) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/rights/params/"+paramId);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).delete(String.class);
        return t;
    }

    /**
     * Vytvoreni prava - 1
     * @return
     * @throws JSONException 
     */
    private static String createSampleRight() throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("action", "read");
		jsonObj.put("pid", "uuid:1");
		jsonObj.put("role", new JSONObject(UsersAndRolesClient.role(3)));
		
		System.out.println(jsonObj);
		
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/rights");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(jsonObj.toString()).post(String.class);
        return t;
    }


    /**
     * Vytvoreni prava - 2
     * @param critqname
     * @param param
     * @return
     * @throws JSONException 
     */
    private static String createSampleRight2(String critqname,JSONObject param) throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("action", "read");
		jsonObj.put("pid", "uuid:1");
		jsonObj.put("role", new JSONObject(UsersAndRolesClient.role(3)));
		
		JSONObject critJSON = new JSONObject();
		critJSON.put("qname", critqname);
		critJSON.put("params", param);
		
		jsonObj.put("criterium", critJSON);
		
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/admin/rights");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(jsonObj.toString()).post(String.class);
        return t;
    }


}
