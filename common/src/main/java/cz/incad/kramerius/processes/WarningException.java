package cz.incad.kramerius.processes;

/**
 * Warning signal
 * @author pavels
 */
public class WarningException extends RuntimeException {


	public WarningException() {
		super();
	}

	public WarningException(String message, Throwable cause) {
		super(message, cause);
	}

	public WarningException(String message) {
		super(message);
	}

	public WarningException(Throwable cause) {
		super(cause);
	}
}
