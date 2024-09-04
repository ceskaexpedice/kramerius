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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.apiNew.client.v70.ClientKeycloakConfig;

/**
 */
@Path("exts/v7.0/tokens")
public class ExtsTokensResource {


    @GET
    @Path("{clientid}")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response token(@PathParam("clientid") String clientid, @QueryParam("secrets") String secrets) {
        try {
        
            String path = WORKING_DIR + "/keycloak.json";
            String str = IOUtils.toString(new FileInputStream(path),"UTF-8");
            ClientKeycloakConfig cnf = ClientKeycloakConfig.load(new JSONObject(str));

            String type = "application/x-www-form-urlencoded; charset=UTF-8";

            StringBuilder builder = new StringBuilder();
            builder.append(cnf.getAuthServer());
            if (!builder.toString().endsWith("/")) builder.append("/");
            builder.append("realms/");
            builder.append(cnf.getRealm());
            builder.append("/protocol/openid-connect/token");
            
            Client client = Client.create();
            WebResource webResource = client.resource(builder.toString());
            
            MultivaluedMapImpl<String, String> values = new MultivaluedMapImpl();
            values.add("grant_type", "client_credentials");
            values.add("client_secret", secrets);
            values.add("client_id", clientid);

            ClientResponse response = webResource.type(type).post(ClientResponse.class, values);
            String entity = (String) response.getEntity(String.class);
            return Response.status(response.getStatus()).entity(entity.toString()).build();

        } catch (Exception e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    
}
