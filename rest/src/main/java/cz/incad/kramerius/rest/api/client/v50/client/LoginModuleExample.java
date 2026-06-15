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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;

/**
 * Example of login in application - Jersey 3 / Jakarta
 */
public class LoginModuleExample {

	public static boolean checkLogin(String checkingLoginName, String pswd) throws JSONException {
		try (Client client = ClientBuilder.newBuilder().build()) {
			WebTarget target = client.target("http://localhost:8080/search/api/v5.0/user");

			// Add basic auth filter
			client.register(new BasicAuthenticationFilter(checkingLoginName, pswd));

			try (Response response = target.request(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.get()) {
				String jsonText = response.readEntity(String.class);
				JSONObject jsonObject = new JSONObject(jsonText);

				if (jsonObject.has("lname")) {
					String loginName = jsonObject.getString("lname");
					return loginName.equals(checkingLoginName);
				} else {
					return false;
				}
			}
		}
	}

	public static void main(String[] args) throws JSONException {
		boolean validLogin = checkLogin("krameriusAdmin", "krameriusAdmin");
		System.out.println("is valid login name and password? " + validLogin);

		boolean invalidLogin = checkLogin("krystof", "harant");
		System.out.println("is valid login name and password? " + invalidLogin);
	}
}