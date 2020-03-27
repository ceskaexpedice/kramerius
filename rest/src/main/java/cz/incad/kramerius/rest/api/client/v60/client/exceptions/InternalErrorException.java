package cz.incad.kramerius.rest.api.client.v60.client.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class InternalErrorException extends ApiException {

    public InternalErrorException(String errorMessage) {
        super(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(buildErrorJson(errorMessage))
                .build());
    }

}
