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

import javax.ws.rs.core.MediaType;

import net.sf.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class RightsClient {
	
	public static final String DEFAULT_NAME="k4_admin";
	public static final String DEFAULT_PSWD="k4_admin";
	
    public static String deleteRight(String delId) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/k5/admin/rights/"+delId);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).delete(String.class);
        return t;
    }
    
    public static String createRight(JSONObject jsonObj) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/k5/admin/rights");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(jsonObj.toString()).post(String.class);
        return t;
    }
    
    public static String rights() {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/k5/admin/rights");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    public static String right(String id) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/k5/admin/rights/"+id);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    public static String params() {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/k5/admin/rights/params");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    public static String param(String paramId) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/k5/admin/rights/params/"+paramId);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    public static String createParam(JSONObject json) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/k5/admin/rights/params");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(json.toString()).post(String.class);
        return t;
    }

    public static String deleteParam(String paramId) {
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/k5/admin/rights/params/"+paramId);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    private static String createSampleRight() {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("action", "read");
		jsonObj.put("pid", "uuid:1");
		jsonObj.put("role", JSONObject.fromObject(UsersClient.role("3")));

    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/k5/admin/rights");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(jsonObj.toString()).post(String.class);
        return t;
    }


    private static String createSampleRight2(String critqname,JSONObject param) {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("action", "read");
		jsonObj.put("pid", "uuid:1");
		jsonObj.put("role", JSONObject.fromObject(UsersClient.role("3")));
		
		JSONObject critJSON = new JSONObject();
		critJSON.put("qname", critqname);
		critJSON.put("params", param);
		
		jsonObj.put("criterium", critJSON);
		
    	Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/k5/admin/rights");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(jsonObj.toString()).post(String.class);
        return t;
    }


    public static void main(String[] args) {
//    	String l = params();
//    	System.out.println(l);
    	
//    	String deleteRight = deleteRight("46");
//    	System.out.println(deleteRight);
//    	System.out.println("======>");
//    	String created = createSampleRight2("cz.incad.kramerius.security.impl.criteria.Window", JSONObject.fromObject(param("1")));
//    	System.out.println(created);
//    	String deleted = deleteRight(JSONObject.fromObject(created).getString("id"));
//    	System.out.println(deleted);
    }
}
