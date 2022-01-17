package cz.incad.kramerius.rest.apiNew.exceptions;

import javax.ws.rs.core.Response;

public class InternalErrorException extends ApiException {

    public InternalErrorException() {
        super(Response.Status.INTERNAL_SERVER_ERROR);
    }

    public InternalErrorException(String message) {
        super(Response.Status.INTERNAL_SERVER_ERROR, message);
    }

    public InternalErrorException(String messageTemplate, Object... messageArgs) {
        super(Response.Status.INTERNAL_SERVER_ERROR, messageTemplate, messageArgs);
    }

}
