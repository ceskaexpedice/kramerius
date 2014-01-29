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
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

/**
 * Ziskava informace o titulu
 * @author pavels
 */
public class ItemResourceClient {

	/**
	 * Ziska informace o titulu
	 * @param pid
	 * @return
	 */
	public static String item(String pid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/item/" + pid );
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
	}
	
	/**
	 * Ziska seznam deti titulu
	 * @param pid
	 * @return
	 */
	public static String children(String pid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/item/" + pid +"/children");
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
	}
	
	/**
	 * Ziska seznam sourozencu
	 * @param pid
	 * @return
	 */
	public static String siblings(String pid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/item/" + pid +"/siblings");
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
	}

	/**
	 * Ziska seznam vsech streamu
	 * @param pid
	 * @return
	 */
	public static String streams(String pid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/item/" + pid +"/streams");
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
	}
	
	/**
	 * Ziska data streamu
	 * @param pid
	 * @param stream
	 * @return
	 */
	public static String stream(String pid, String stream) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/item/" + pid +"/streams/"+stream);
        String t = r.accept(MediaType.MEDIA_TYPE_WILDCARD).get(String.class);
        return t;
	}

	/**
	 * Ziska obrazek IMG_FULL 
	 * @param pid
	 * @return
	 */
	public static byte[] full(String pid) {
        Client c = Client.create();
        c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);        
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/item/" + pid +"/full");
        byte[] t = r.accept(MediaType.MEDIA_TYPE_WILDCARD).get(new byte[0].getClass());
        return t;
	}

	/**
	 * Ziska obrazek IMG_PREVIEW 
	 * @param pid
	 * @return
	 */
	public static byte[] preview(String pid) {
        Client c = Client.create();
        c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);        
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/item/" + pid +"/preview");
        byte[] t = r.accept(MediaType.MEDIA_TYPE_WILDCARD).get(new byte[0].getClass());
        return t;
	}

	/**
	 * Ziska obrazek IMG_THUMB 
	 * @param pid
	 * @return
	 */
	public static byte[] thumb(String pid) {
        Client c = Client.create();
        c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);        
        WebResource r = c.resource("http://localhost:8080/search/api/v5.0/item/" + pid +"/thumb");
        byte[] t = r.accept(MediaType.MEDIA_TYPE_WILDCARD).get(new byte[0].getClass());
        return t;
	}

	public static void main(String[] args) throws IOException {
		String item = item("uuid:045b1250-7e47-11e0-add1-000d606f5dc6");
		System.out.println(item);

		String chilren = children("uuid:045b1250-7e47-11e0-add1-000d606f5dc6");
		System.out.println(chilren);
		
		String streams = streams("uuid:045b1250-7e47-11e0-add1-000d606f5dc6");
		System.out.println(streams);
		
		String dcStream = stream("uuid:045b1250-7e47-11e0-add1-000d606f5dc6", "DC");
		System.out.println(dcStream);
		
		byte[] full = full("uuid:045b1250-7e47-11e0-add1-000d606f5dc6");
		BufferedImage readImage = ImageIO.read(new ByteArrayInputStream(full));
		System.out.println("image size "+readImage.getHeight()+"x"+readImage.getWidth());
		
	}
}
