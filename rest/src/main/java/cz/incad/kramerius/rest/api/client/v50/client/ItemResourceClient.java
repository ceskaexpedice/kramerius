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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;

/**
 * Access item info - Jersey 3 / Jakarta
 * @author pavels
 */
public class ItemResourceClient {

	private static Client createClient() {
		// enable automatic redirects
		Client client = ClientBuilder.newBuilder()
				.build();
		client.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
		return client;
	}

	public static String item(String pid) {
		try (Client client = createClient()) {
			WebTarget target = client.target("http://localhost:8080/search/api/v5.0/item/" + pid);
			try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
				return response.readEntity(String.class);
			}
		}
	}

	public static String children(String pid) {
		try (Client client = createClient()) {
			WebTarget target = client.target("http://localhost:8080/search/api/v5.0/item/" + pid + "/children");
			try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
				return response.readEntity(String.class);
			}
		}
	}

	public static String siblings(String pid) {
		try (Client client = createClient()) {
			WebTarget target = client.target("http://localhost:8080/search/api/v5.0/item/" + pid + "/siblings");
			try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
				return response.readEntity(String.class);
			}
		}
	}

	public static String streams(String pid) {
		try (Client client = createClient()) {
			WebTarget target = client.target("http://localhost:8080/search/api/v5.0/item/" + pid + "/streams");
			try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
				return response.readEntity(String.class);
			}
		}
	}

	public static String stream(String pid, String stream) {
		try (Client client = createClient()) {
			WebTarget target = client.target("http://localhost:8080/search/api/v5.0/item/" + pid + "/streams/" + stream);
			try (Response response = target.request(MediaType.WILDCARD).get()) {
				return response.readEntity(String.class);
			}
		}
	}

	public static byte[] full(String pid) {
		return fetchImage(pid, "full");
	}

	public static byte[] preview(String pid) {
		return fetchImage(pid, "preview");
	}

	public static byte[] thumb(String pid) {
		return fetchImage(pid, "thumb");
	}

	private static byte[] fetchImage(String pid, String type) {
		try (Client client = createClient()) {
			WebTarget target = client.target("http://localhost:8080/search/api/v5.0/item/" + pid + "/" + type);
			try (Response response = target.request(MediaType.WILDCARD).get()) {
				return response.readEntity(byte[].class);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		String pid = "uuid:045b1250-7e47-11e0-add1-000d606f5dc6";

		String item = item(pid);
		System.out.println(item);

		String children = children(pid);
		System.out.println(children);

		String streams = streams(pid);
		System.out.println(streams);

		String dcStream = stream(pid, "DC");
		System.out.println(dcStream);

		byte[] fullImg = full(pid);
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(fullImg));
		System.out.println("Image size: " + img.getHeight() + "x" + img.getWidth());
	}
}