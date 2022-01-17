package cz.incad.kramerius.rest.apiNew.exceptions;

import javax.ws.rs.core.Response;

public class ForbiddenException extends ApiException {

    public ForbiddenException() {
        super(Response.Status.FORBIDDEN);
    }

    public ForbiddenException(String message) {
        super(Response.Status.FORBIDDEN, message);
    }

    public ForbiddenException(String messageTemplate, Object... messageArgs) {
        super(Response.Status.FORBIDDEN, messageTemplate, messageArgs);
    }

}
