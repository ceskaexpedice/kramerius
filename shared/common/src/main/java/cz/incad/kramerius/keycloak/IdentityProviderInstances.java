package cz.incad.kramerius.keycloak;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

public interface IdentityProviderInstances {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<Map<String, Object>> list(@QueryParam("first") int first,
                                   @QueryParam("max") int max,
                                   @QueryParam("briefRepresentation") boolean briefRepresentation);
}
