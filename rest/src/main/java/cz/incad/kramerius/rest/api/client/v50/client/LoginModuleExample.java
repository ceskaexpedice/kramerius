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

import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.BasicAuthenticationFilter;

/**
 * Example of login in application
 * @author pavels
 */
public class LoginModuleExample {
	
	public static boolean checkLogin(String checkingLoginName, String pswd) throws JSONException{ 
		// get info from userResource
		Client c = Client.create();

		WebResource r = c.resource("http://localhost:8080/search/api/v5.0/user");
		r.addFilter(new BasicAuthenticationFilter(checkingLoginName, pswd));
		String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(String.class);
		
		JSONObject jsonObject = new JSONObject(t);
		if (jsonObject.has("lname")) {
			String loginName = jsonObject.getString("lname");
			if (loginName.equals(checkingLoginName)) {
				return true;
			} else return false;
		} else return false;
	}
	
	public static void main(String[] args) throws JSONException {
		boolean validLogin = checkLogin("krameriusAdmin", "krameriusAdmin");
		System.out.println("is valid login name and password ? "+validLogin);

		boolean invalidLogin = checkLogin("krystof", "harant");
		System.out.println("is valid login name and password ? "+invalidLogin);

	}
	
}
