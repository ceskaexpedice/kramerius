package cz.incad.kramerius.rest.api.exceptions;

import javax.servlet.http.HttpServletResponse;

public class UnauthorizedException extends AbstractRestJSONException {
    public UnauthorizedException(String message) {
        super(message, HttpServletResponse.SC_UNAUTHORIZED);
    }

    public UnauthorizedException() {
        super("not logged in", HttpServletResponse.SC_UNAUTHORIZED);
    }
}
