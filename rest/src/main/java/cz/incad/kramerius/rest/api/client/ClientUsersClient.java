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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.core.MediaType;

import net.sf.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.security.utils.PasswordDigest;

public class ClientUsersClient {

	private static final String DEFAULT_NAME = "krameriusAdmin";
	private static final String DEFAULT_PSWD = "krameriusAdmin";

	public static String savePassword() {
		Client c = Client.create();

		// http://localhost:8080/k5velocity-1.0-SNAPSHOT/api/user
		// http://localhost:8080/search/api/v4.6/k5/user
		// "http://localhost:8080/search/api/v4.6/k5/admin/users"
		WebResource r = c
				.resource("http://localhost:8080/search/api/v4.6/k5/user");
		r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
		JSONObject object = new JSONObject();
		object.put("pswd", "krameriusAdmin");

		String t = r.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.entity(object.toString(), MediaType.APPLICATION_JSON)
				.post(String.class);
		return t;
	}

	public static String getUser() {
		Client c = Client.create();

		// http://localhost:8080/k5velocity-1.0-SNAPSHOT/api/user
		// http://localhost:8080/search/api/v4.6/k5/user
		// "http://localhost:8080/search/api/v4.6/k5/admin/users"
		WebResource r = c
				.resource("http://localhost:8080/search/api/v4.6/k5/user");
		r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
		JSONObject object = new JSONObject();
		object.put("pswd", "krameriusAdmin");

		String t = r.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(String.class);
		return t;
	}

	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String t = getUser();
		System.out.println(t);
		String st = savePassword();
		System.out.println(st);

//		String t = PasswordDigest.messageDigest( "krameriusAdmin");
//		System.out.println(t);
		
	}
}
