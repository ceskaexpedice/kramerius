package cz.incad.kramerius.rest.apiNew.exceptions;

public class ProxyAuthenticationRequiredException extends ApiException {

    public ProxyAuthenticationRequiredException() {
        super(407);
    }

    public ProxyAuthenticationRequiredException(String message) {
        super(407, message);
    }

    public ProxyAuthenticationRequiredException(String messageTemplate, Object... messageArgs) {
        super(407, messageTemplate, messageArgs);
    }

}
