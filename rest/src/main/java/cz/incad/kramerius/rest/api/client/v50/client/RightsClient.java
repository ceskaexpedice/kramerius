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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;

/**
 * Informace o pravech pro prave komunikujiciho uzivatele
 */
public class RightsClient {

	private static final String DEFAULT_NAME = "krameriusAdmin";
	private static final String DEFAULT_PSWD = "krameriusAdmin";

	/**
	 * Globalni prava - prava na urovni repozitare
	 */
	public static String globalRights() {
		try (Client client = ClientBuilder.newBuilder().build()) {
			client.register(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
			WebTarget target = client.target("http://localhost:8080/search/api/v5.0/rights");

			try (Response response = target.request(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.get()) {
				return response.readEntity(String.class);
			}
		}
	}

	/**
	 * Konkretni prava pro vybrane akce a vybrany pid
	 */
	public static String concreteObjects(List<String> actions, String pid) {
		try (Client client = ClientBuilder.newBuilder().build()) {
			client.register(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));

			String actionParam = String.join(",", actions);
			WebTarget target = client.target("http://localhost:8080/search/api/v5.0/rights")
					.queryParam("actions", actionParam)
					.queryParam("pid", pid);

			try (Response response = target.request(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.get()) {
				return response.readEntity(String.class);
			}
		}
	}

	public static void main(String[] args) throws JSONException {
		JSONObject jsonObj = new JSONObject(globalRights());
		Iterator<String> actions = jsonObj.keys();
		System.out.println("Right for user " + DEFAULT_NAME);
		while (actions.hasNext()) {
			String act = actions.next();
			System.out.println("\t'" + act + "' = " + jsonObj.getBoolean(act));
		}

		String pid = "uuid:045b1250-7e47-11e0-add1-000d606f5dc6";
		System.out.println("Right for user " + DEFAULT_NAME + " and for object pid " + pid);
		jsonObj = new JSONObject(concreteObjects(Arrays.asList("read","administrate"), pid));
		actions = jsonObj.keys();
		while (actions.hasNext()) {
			String act = actions.next();
			System.out.println("\t'" + act + "' = " + jsonObj.getBoolean(act));
		}
	}
}