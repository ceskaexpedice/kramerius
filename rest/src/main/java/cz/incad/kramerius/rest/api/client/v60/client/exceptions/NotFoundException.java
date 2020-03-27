package cz.incad.kramerius.rest.api.client.v60.client.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class NotFoundException extends ApiException {

    public NotFoundException() {
        super(Response.status(Response.Status.NOT_FOUND).build());
    }

    public NotFoundException(String pid) {
        super(Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(buildErrorJson(String.format("object with pid %s not found in repository", pid)))
                .build());
    }


}
