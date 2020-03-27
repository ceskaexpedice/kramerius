package cz.incad.kramerius.rest.apiNew.exceptions;

import javax.ws.rs.core.Response;

public class BadRequestException extends ApiException {

    public BadRequestException() {
        super(Response.Status.FORBIDDEN);
    }

    public BadRequestException(String message) {
        super(Response.Status.FORBIDDEN, message);
    }

    public BadRequestException(String messageTemplate, Object... messageArgs) {
        super(Response.Status.FORBIDDEN, messageTemplate, messageArgs);
    }

}
