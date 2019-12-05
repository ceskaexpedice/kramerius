/*
 * Copyright (C) 2012 Pavel Stastny
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
package cz.incad.kramerius.utils;

import java.util.ArrayList;
import java.util.List;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.Base64;

/**
 * @author pavels
 *
 */
public class BasicAuthenticationFilter extends ClientFilter {
    
    public BasicAuthenticationFilter(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
        
        encodeUserAndPass(clientRequest, this.username, this.password);

        return getNext().handle(clientRequest);
    }

    public static void encodeUserAndPass(ClientRequest clientRequest, String unm,
            String pswd) {
        // encode the password
        byte[] encoded = Base64.encode((unm + ":" + pswd).getBytes());

        // add the header
        List<Object> headerValue = new ArrayList<Object>();
        headerValue.add("Basic " + new String(encoded));
        clientRequest.getMetadata().put("Authorization", headerValue);
    }

    private String username;
    private String password;

}
