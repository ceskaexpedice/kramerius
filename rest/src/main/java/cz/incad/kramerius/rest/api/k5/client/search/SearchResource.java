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
package cz.incad.kramerius.rest.api.k5.client.search;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.google.inject.Inject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.utils.IOUtils;
import java.net.URLEncoder;
import net.sf.json.JSONObject;

@Path("/k5/search")
public class SearchResource {

    @Inject
    SolrAccess solrAccess;

    @GET
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response select(@Context UriInfo uriInfo) {
        try {
            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            StringBuilder builder = new StringBuilder();
            Set<String> keys = queryParameters.keySet();
            for (String k : keys) {
                for (String v : queryParameters.get(k)) {
                    builder.append(k + "=" + URLEncoder.encode(v, "UTF-8"));
                    builder.append("&");
                }
            }
            InputStream istream = this.solrAccess.request(builder.toString(), "json");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(istream, bos);

            return Response.ok().entity(bos.toByteArray()).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("terms")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response terms(@Context UriInfo uriInfo) {
        try {
            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            StringBuilder builder = new StringBuilder();
            Set<String> keys = queryParameters.keySet();
            for (String k : keys) {
                for (String v : queryParameters.get(k)) {
                    builder.append(k + "=" + URLEncoder.encode(v, "UTF-8"));
                    builder.append("&");
                }
            }
            InputStream istream = this.solrAccess.terms(builder.toString(), "json");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(istream, bos);

            return Response.ok().entity(bos.toByteArray()).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
