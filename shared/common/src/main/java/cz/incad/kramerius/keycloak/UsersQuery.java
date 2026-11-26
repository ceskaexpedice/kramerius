package cz.incad.kramerius.keycloak;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.keycloak.representations.idm.UserRepresentation;

public interface UsersQuery {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("search") String search,
                                    @QueryParam("username") String username,
                                    @QueryParam("firstName") String firstName,
                                    @QueryParam("lastName") String lastName,
                                    @QueryParam("email") String email,
                                    @QueryParam("enabled") Boolean enabled,
                                    @QueryParam("idpAlias") String idpAlias,
                                    @QueryParam("q") String q,
                                    @QueryParam("exact") Boolean exact,
                                    @QueryParam("first") Integer first,
                                    @QueryParam("max") Integer max,
                                    @QueryParam("briefRepresentation") Boolean briefRepresentation);
}
