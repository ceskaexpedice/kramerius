package cz.incad.kramerius.uiconfig;

public class InvalidJsonException extends RuntimeException {
    public InvalidJsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
