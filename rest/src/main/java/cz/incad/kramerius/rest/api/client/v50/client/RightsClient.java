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

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.BasicAuthenticationFilter;

/**
 * Informace o pravech pro prave komunikujiciho uzivatele
 * @author pavels
 */
public class RightsClient {

	private static final String DEFAULT_NAME = "krameriusAdmin";
	private static final String DEFAULT_PSWD = "krameriusAdmin";

	/**
	 * Globalni prava - prava na urovni repozitare
	 * @return
	 */
	public static String globalRights() {
		Client c = Client.create();

		WebResource r = c.resource("http://localhost:8080/search/api/v5.0/rights");
		r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));

		String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
		return t;
	}
	
	
	/**
	 * Konkretni prava pro vybrane akce a vybrany pid
	 * @param actions
	 * @param pid
	 * @return
	 */
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

	
	public static void main(String[] args) throws JSONException {
		JSONObject jsonObj = new JSONObject(globalRights());
		Iterator actions = jsonObj.keys();
		System.out.println("Right for user "+DEFAULT_NAME);
		while(actions.hasNext()) {
			String act = (String) actions.next();
			System.out.println("\t'"+act+"' = "+jsonObj.getBoolean(act));
		}
		
		String pid = "uuid:045b1250-7e47-11e0-add1-000d606f5dc6";
		System.out.println("Right for user "+DEFAULT_NAME+" and for object pid "+pid);
		jsonObj = new JSONObject(concreteObjects(Arrays.asList("read","administrate"), pid));
		actions = jsonObj.keys();
		while(actions.hasNext()) {
			String act = (String) actions.next();
			System.out.println("\t'"+act+"' = "+jsonObj.getBoolean(act));
		}
		
		
	}
}
