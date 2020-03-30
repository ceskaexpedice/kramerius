package cz.incad.kramerius.rest.apiNew.exceptions;

import javax.ws.rs.core.Response;

public class BadRequestException extends ApiException {

    public BadRequestException() {
        super(Response.Status.BAD_REQUEST);
    }

    public BadRequestException(String message) {
        super(Response.Status.BAD_REQUEST, message);
    }

    public BadRequestException(String messageTemplate, Object... messageArgs) {
        super(Response.Status.BAD_REQUEST, messageTemplate, messageArgs);
    }

}
