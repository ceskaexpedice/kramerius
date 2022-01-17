package cz.incad.kramerius.rest.api.exceptions;

import javax.servlet.http.HttpServletResponse;

public class UnauthorizedException extends AbstractRestJSONException {

    public UnauthorizedException(String message) {
        super(message, HttpServletResponse.SC_UNAUTHORIZED);
    }

    public UnauthorizedException(String message, Object... params) {
        super(String.format(message, params), HttpServletResponse.SC_UNAUTHORIZED);
    }
}
