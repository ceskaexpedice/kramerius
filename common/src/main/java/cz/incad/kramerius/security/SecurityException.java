package cz.incad.kramerius.security;

/**
 * Request cannot be processed
 */
public class SecurityException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SecurityException() {
		super();
	}

	public SecurityException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecurityException(String message) {
		super(message);
	}

	public SecurityException(Throwable cause) {
		super(cause);
	}
	
}
