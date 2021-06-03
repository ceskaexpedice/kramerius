package cz.incad.kramerius.security.labels;

public class LabelsManagerException extends Exception {

    public LabelsManagerException() {
    }

    public LabelsManagerException(String message) {
        super(message);
    }

    public LabelsManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public LabelsManagerException(Throwable cause) {
        super(cause);
    }

    public LabelsManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
