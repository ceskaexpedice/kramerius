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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;

import net.sf.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.rest.api.client.BasicAuthenticationFilter;

public class RightsClient {

	private static final String DEFAULT_NAME = "krameriusAdmin";
	private static final String DEFAULT_PSWD = "krameriusAdmin";

	public static String globalRights() {
		Client c = Client.create();

		WebResource r = c.resource("http://localhost:8080/search/api/v5.0/rights");
		r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));

		String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
		return t;
	}

	public static String concreteObjects(List<String> actions, String pid) {
		Client c = Client.create();
		StringBuilder builder = new StringBuilder();
		for (int i = 0,ll=actions.size(); i < ll; i++) {
			if (i>0) builder.append(",");
			builder.append(actions.get(i));
		}
		WebResource r = c.resource("http://localhost:8080/search/api/v5.0/rights?actions="+builder.toString()+"&pid="+pid);
		r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
		String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
		return t;
	}

	
	public static void main(String[] args) {
		JSONObject jsonObj = JSONObject.fromObject(globalRights());
		Iterator actions = jsonObj.keySet().iterator();
		System.out.println("Right for user "+DEFAULT_NAME);
		while(actions.hasNext()) {
			String act = (String) actions.next();
			System.out.println("\t'"+act+"' = "+jsonObj.getBoolean(act));
		}
		
		String pid = "uuid:045b1250-7e47-11e0-add1-000d606f5dc6";
		System.out.println("Right for user "+DEFAULT_NAME+" and for object pid "+pid);
		jsonObj = JSONObject.fromObject(concreteObjects(Arrays.asList("read","administrate"), pid));
		actions = jsonObj.keySet().iterator();
		while(actions.hasNext()) {
			String act = (String) actions.next();
			System.out.println("\t'"+act+"' = "+jsonObj.getBoolean(act));
		}
		
		
	}
}
