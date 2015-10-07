package cz.incad.kramerius.rest.api.exceptions;

import javax.servlet.http.HttpServletResponse;

public class BadRequestException extends AbstractRestJSONException {

    public BadRequestException(String message, Exception ex) {
        super(message, ex, HttpServletResponse.SC_BAD_REQUEST);
    }

    public BadRequestException(String message) {
        super(message, HttpServletResponse.SC_BAD_REQUEST);
    }

}
