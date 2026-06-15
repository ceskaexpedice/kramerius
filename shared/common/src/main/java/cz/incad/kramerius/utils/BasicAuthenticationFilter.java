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

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Provider
public class BasicAuthenticationFilter implements ClientRequestFilter {

    private final String username;
    private final String password;

    public BasicAuthenticationFilter(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void filter(ClientRequestContext request) throws IOException {
        encodeUserAndPass(request, username, password);
    }

    public static void encodeUserAndPass(ClientRequestContext request, String unm, String pswd) {
        // encode the password
        String token = Base64.getEncoder()
                .encodeToString((unm + ":" + pswd).getBytes(StandardCharsets.UTF_8));

        // add the header
        request.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Basic " + token);
    }
}
