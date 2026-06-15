/*
 * Copyright (C) Sep 3, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.apiNew.exts.v70;

import static cz.incad.kramerius.Constants.WORKING_DIR;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import cz.incad.kramerius.auth.ClientKeycloakConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;

/**
 */
@Path("exts/v7.0/tokens")
public class ExtsTokensResource {

    @GET
    @Path("{clientid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response token(@PathParam("clientid") String clientid,
                          @QueryParam("secrets") String secrets) {
        try {
            // Load Keycloak config
            String path = WORKING_DIR + "/keycloak.json";
            String json = IOUtils.toString(
                    new FileInputStream(path),
                    StandardCharsets.UTF_8
            );
            ClientKeycloakConfig cnf =
                    ClientKeycloakConfig.load(new JSONObject(json));

            String tokenUrl = cnf.getAuthServer();
            if (!tokenUrl.endsWith("/")) {
                tokenUrl += "/";
            }
            tokenUrl += "realms/" + cnf.getRealm()
                    + "/protocol/openid-connect/token";

            // Build form data
            MultivaluedHashMap<String, String> form =
                    new MultivaluedHashMap<>();
            form.add("grant_type", "client_credentials");
            form.add("client_id", clientid);
            form.add("client_secret", secrets);

            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(tokenUrl);

            Response kcResponse = target
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.form(form));

            String entity = kcResponse.readEntity(String.class);

            return Response
                    .status(kcResponse.getStatus())
                    .entity(entity)
                    .build();

        } catch (Exception e) {
            throw new GenericApplicationException(e.getMessage(), e);
        }
    }
}