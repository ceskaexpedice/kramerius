package cz.incad.kramerius.rest.apiNew.exceptions;

import javax.ws.rs.core.Response;

public class NotFoundException extends ApiException {

    public NotFoundException() {
        super(Response.Status.NOT_FOUND);
    }

    public NotFoundException(String message) {
        super(Response.Status.NOT_FOUND, message);
    }

    public NotFoundException(String messageTemplate, Object... messageArgs) {
        super(Response.Status.NOT_FOUND, messageTemplate, messageArgs);
    }

}
