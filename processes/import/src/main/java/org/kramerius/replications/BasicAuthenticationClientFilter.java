package org.kramerius.replications;

import java.util.ArrayList;
import java.util.List;


import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.Base64;

public class BasicAuthenticationClientFilter extends ClientFilter {

    public BasicAuthenticationClientFilter(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {

        // encode the password
        byte[] encoded =  Base64.encode((username + ":" + password).getBytes());

        // add the header
        List<Object> headerValue = new ArrayList<Object>();
        headerValue.add("Basic " + new String(encoded));
        clientRequest.getMetadata().put("Authorization", headerValue);

        return getNext().handle(clientRequest);
    }

    private String username;
    private String password;
}
