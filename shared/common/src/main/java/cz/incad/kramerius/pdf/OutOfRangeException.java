package cz.incad.kramerius.pdf;
public class OutOfRangeException extends Exception {

    private static final long serialVersionUID = 1L;

    public OutOfRangeException() {
        super();
    }


    public OutOfRangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public OutOfRangeException(String message) {
        super(message);
    }

    public OutOfRangeException(Throwable cause) {
        super(cause);
    }
}
