package cz.incad.kramerius.utils;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Provider
public class BasicAuthenticationClientFilter implements ClientRequestFilter {

    private final String username;
    private final String password;

    public BasicAuthenticationClientFilter(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void filter(ClientRequestContext request) throws IOException {
        String token = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));

        request.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Basic " + token);
    }

}