package cz.incad.kramerius.security.licenses;

/**
 * Represents a license handling error 
 * @author happy
 */
public class LicensesManagerException extends Exception {

    public LicensesManagerException() {
    }

    public LicensesManagerException(String message) {
        super(message);
    }

    public LicensesManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public LicensesManagerException(Throwable cause) {
        super(cause);
    }

    public LicensesManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
