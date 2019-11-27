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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.BasicAuthenticationFilter;

/**
 * Informace o uzivateli
 * @author pavels
 */
public class ClientUsersClient {

	private static final String DEFAULT_NAME = "krameriusAdmin";
	private static final String DEFAULT_PSWD = "krameriusAdmin";

	/** Save new password 
	 * @throws JSONException */
	public static String savePassword() throws JSONException {
		Client c = Client.create();

		WebResource r = c.resource("http://localhost:8080/search/api/v5.0/user");
		r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
		JSONObject object = new JSONObject();
		object.put("pswd", "krameriusAdmin");

		String t = r.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.entity(object.toString(), MediaType.APPLICATION_JSON)
				.post(String.class);
		return t;
	}
	
	/**
	 * Get user info
	 * @return
	 * @throws JSONException 
	 */
	public static String getUser() throws JSONException {
		Client c = Client.create();

		WebResource r = c.resource("http://localhost:8080/search/api/v5.0/user");
		r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
		JSONObject object = new JSONObject();
		object.put("pswd", "krameriusAdmin");

		String t = r.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(String.class);
		return t;
	}
	
	/**
	 * Get profile
	 * @return
	 * @throws JSONException 
	 */
	public static String getProfile() throws JSONException {
		Client c = Client.create();
		WebResource r = c.resource("http://localhost:8080/search/api/v5.0/user/profile");
		r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
		JSONObject object = new JSONObject();
		object.put("pswd", "krameriusAdmin");
		String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
		return t;
	}

	/**
	 * Save profile
	 * @return
	 * @throws JSONException 
	 */
	public static void saveProfile(JSONObject profile) throws JSONException {
		Client c = Client.create();
		WebResource r = c.resource("http://localhost:8080/search/api/v5.0/user/profile");
		r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
		JSONObject object = new JSONObject();
		object.put("pswd", "krameriusAdmin");
		String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(profile.toString()).post(String.class);
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
