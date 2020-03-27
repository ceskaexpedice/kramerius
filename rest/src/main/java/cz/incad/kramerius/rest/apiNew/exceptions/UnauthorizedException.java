package cz.incad.kramerius.rest.apiNew.exceptions;

import javax.ws.rs.core.Response;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException() {
        super(Response.Status.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(Response.Status.UNAUTHORIZED, message);
    }

    public UnauthorizedException(String messageTemplate, Object... messageArgs) {
        super(Response.Status.UNAUTHORIZED, messageTemplate, messageArgs);
    }

}
