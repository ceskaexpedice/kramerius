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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * Inforace o virtualni sbirce
 * @author pavels
 *
 */
public class VirtualCollectionsResourceClient {
	
	/**
	 * Seznam vsech virtualnich sbirek
	 * @return
	 */
	public static String vcs() {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/vc");
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
	}

	/**
	 * Konkretni virtualni sbirka
	 * @param vcpid
	 * @return
	 */
	public static String vc(String vcpid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/vc/"+vcpid);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
	}

	/**
	 * Info o objektu
	 * @param vcpid
	 * @return
	 */
	public static String vcAsFedoraObject(String vcpid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/item/"+vcpid);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
	}

	/**
	 * Seznam vsech streamu objektu reprezentujici virtualni sbirku
	 * @param vcpid
	 * @return
	 */
	public static String vcAsStreamsObject(String vcpid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/item/"+vcpid+"/streams");
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
	}

	/**
	 * Ziskani streamu z objektu virtualni sbirky
	 * @param vcpid
	 * @param str
	 * @return
	 */
	public static String vcAsStreamObject(String vcpid, String str) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/item/"+vcpid+"/streams/"+str);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
	}

	public static void main(String[] args) {
		String vcs = vcs();
		System.out.println(vcs);
		
		String vc = vc("vc:f73dee31-ae76-4dbc-b7b9-d986df497596");
		System.out.println(vc);

		String vcaf = vcAsFedoraObject("vc:f73dee31-ae76-4dbc-b7b9-d986df497596");
		System.out.println(vcaf);

		String vcStreams = vcAsStreamsObject("vc:f73dee31-ae76-4dbc-b7b9-d986df497596");
		System.out.println(vcStreams);

		String vcDCSteram = vcAsStreamObject("vc:f73dee31-ae76-4dbc-b7b9-d986df497596","DC");
		System.out.println(vcDCSteram);

	}
	
}
