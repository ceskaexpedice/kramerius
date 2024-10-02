/*
 * Copyright (C) Sep 28, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.apiNew.client.v70.res;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

@Path("/client/v7.0/embedded/files")
public class EmbeddedFilesResource {

    public static final String OPENAPI_PREFIX = "openapi";

    private static final Map<String, String> mimeTypeMap = new HashMap<>();

    static {
        mimeTypeMap.put("js", "application/javascript");
        mimeTypeMap.put("html", "text/html");
        mimeTypeMap.put("map", "application/json");
        mimeTypeMap.put("css", "text/css");
        mimeTypeMap.put("png", "image/png");
        mimeTypeMap.put("yaml", "application/x-yaml");
        mimeTypeMap.put("yml", "application/x-yaml");
        mimeTypeMap.put("openapi", "application/vnd.oai.openapi");
    }

    @GET
    @Path("/{path: (.+)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEmbbededFile(@PathParam("path") String path) {
        try {
            URL resource  = this.getClass().getClassLoader().getResource("/"+OPENAPI_PREFIX + File.separator + path);
            if (resource != null) {
                String content = IOUtils.toString(resource.openStream(), "UTF-8");
                String file = resource.getFile();
                if (file.contains(".")) {
                    String postfix = file.substring(file.lastIndexOf('.')+1);
                    if (mimeTypeMap.containsKey(postfix)) {
                        return Response.ok().entity(content).type(mimeTypeMap.get(postfix)+ "; charset=UTF-8").build();
                    } else {
                        return Response.ok().entity(content).type("text/html; charset=UTF-8").build();
                    }
                } else {
                    return Response.ok().entity(content).type("text/html; charset=UTF-8").build();
                }
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
