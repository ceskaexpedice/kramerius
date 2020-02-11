package cz.incad.kramerius.rest.api.exceptions;

import javax.servlet.http.HttpServletResponse;

public class ProxyAuthenticationRequiredException extends AbstractRestJSONException {

    public ProxyAuthenticationRequiredException(String message) {
        super(message, HttpServletResponse.SC_BAD_REQUEST);
    }

    public ProxyAuthenticationRequiredException(String message, Exception ex) {
        super(message, ex, HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED);
    }

    public ProxyAuthenticationRequiredException(String message, Object... params) {
        super(String.format(message, params), HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED);
    }
}
