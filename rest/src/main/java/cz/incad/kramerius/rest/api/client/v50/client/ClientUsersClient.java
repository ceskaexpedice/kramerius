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
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * User information management - Jersey 3 / Jakarta
 */
public class ClientUsersClient {

	private static final String DEFAULT_NAME = "krameriusAdmin";
	private static final String DEFAULT_PSWD = "krameriusAdmin";

	private static Client createClient() {
		return ClientBuilder.newBuilder()
				.register(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD))
				.build();
	}

	/** Save new password */
	public static String savePassword() throws JSONException {
		Client client = createClient();
		WebTarget target = client.target("http://localhost:8080/search/api/v5.0/user");

		JSONObject object = new JSONObject();
		object.put("pswd", "krameriusAdmin");

		try (Response response = target.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(object.toString(), MediaType.APPLICATION_JSON))) {
			return response.readEntity(String.class);
		} finally {
			client.close();
		}
	}

	/** Get user info */
	public static String getUser() throws JSONException {
		Client client = createClient();
		WebTarget target = client.target("http://localhost:8080/search/api/v5.0/user");

		try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
			return response.readEntity(String.class);
		} finally {
			client.close();
		}
	}

	/** Get profile */
	public static String getProfile() throws JSONException {
		Client client = createClient();
		WebTarget target = client.target("http://localhost:8080/search/api/v5.0/user/profile");

		try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
			return response.readEntity(String.class);
		} finally {
			client.close();
		}
	}

	/** Save profile */
	public static void saveProfile(JSONObject profile) throws JSONException {
		Client client = createClient();
		WebTarget target = client.target("http://localhost:8080/search/api/v5.0/user/profile");

		try (Response response = target.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(profile.toString(), MediaType.APPLICATION_JSON))) {
			response.readEntity(String.class); // optionally consume response
		} finally {
			client.close();
		}
	}

	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException, JSONException {
		String t = getUser();
		System.out.println(t);

		String st = savePassword();
		System.out.println(st);

		String profile = getProfile();
		JSONObject jsonProfile = new JSONObject(profile);
		System.out.println(jsonProfile);

		jsonProfile.put("myproperty", "myvalue");
		saveProfile(jsonProfile);
	}
}