package cz.incad.kramerius.keycloak;

import org.keycloak.representations.idm.EventRepresentation;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

/** Keycloak Admin REST user-event query compatible with the Keycloak 20 API. */
public interface KeycloakUserEventsQuery {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<EventRepresentation> search(@QueryParam("type") List<String> types,
                                     @QueryParam("user") String userId,
                                     @QueryParam("dateFrom") String dateFrom,
                                     @QueryParam("dateTo") String dateTo,
                                     @QueryParam("first") Integer first,
                                     @QueryParam("max") Integer max);
}
