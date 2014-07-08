package cz.incad.kramerius.processes;

public class ProcessManipulationException extends Exception {

    public ProcessManipulationException() {
        super();
    }

    /*
    public ProcessManipulationException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }*/

    public ProcessManipulationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessManipulationException(String message) {
        super(message);
    }

    public ProcessManipulationException(Throwable cause) {
        super(cause);
    }
}
