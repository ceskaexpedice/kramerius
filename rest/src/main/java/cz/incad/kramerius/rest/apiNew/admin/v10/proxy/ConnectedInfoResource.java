package cz.incad.kramerius.rest.apiNew.admin.v10.proxy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.inject.Inject;

import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;

@Path("/admin/v7.0/connected")
public class ConnectedInfoResource {
	
	@Inject
	private Instances libraries;

		
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllConnected() {
    	JSONObject retval = new JSONObject();
    	this.libraries.allInstances().forEach(library-> {
    		retval.put(library, libraryJSON(library));
    	});
        return Response.ok(retval).build();
    }

    private JSONObject libraryJSON(String library) {
    	JSONObject retval = new JSONObject();
    	retval.put("status", this.libraries.getStatus(library));
    	return retval;
    }

	@GET
    @Path("{library}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response library(@PathParam("library") String library) {
        return Response.ok(libraryJSON(library)).build();
    }

	@PUT
    @Path("{library}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response library(@PathParam("library") String library,@QueryParam("status") String status) {
		this.libraries.setStatus(library, Boolean.parseBoolean(status));
		return Response.ok(libraryJSON(library)).build();
    }
	
}
