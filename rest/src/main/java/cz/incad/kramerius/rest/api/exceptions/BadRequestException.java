package cz.incad.kramerius.rest.api.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class BadRequestException extends AbstractRestJSONException {

    public BadRequestException(String message) {
        super(message, HttpServletResponse.SC_BAD_REQUEST);
    }

    public BadRequestException(String message, Object... params) {
        super(String.format(message, params), HttpServletResponse.SC_BAD_REQUEST);
    }

    @Deprecated
    public BadRequestException(String message, Exception ex) {
        super(message, ex, HttpServletResponse.SC_BAD_REQUEST);
    }

}
