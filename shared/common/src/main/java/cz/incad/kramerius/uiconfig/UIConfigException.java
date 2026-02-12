package cz.incad.kramerius.uiconfig;

/**
 * UIConfigException
 * @author ppodsednik
 */
public class UIConfigException extends RuntimeException {

    public UIConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public UIConfigException(String message) {
        super(message);
    }
}
