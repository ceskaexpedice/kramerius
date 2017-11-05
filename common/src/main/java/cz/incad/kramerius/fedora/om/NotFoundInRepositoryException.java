package cz.incad.kramerius.fedora.om;

/**
 * Created by pstastny on 11/3/2017.
 */
public class NotFoundInRepositoryException extends  RepositoryException  {

    public NotFoundInRepositoryException() {
        super();
    }

    public NotFoundInRepositoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NotFoundInRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundInRepositoryException(String message) {
        super(message);
    }

    public NotFoundInRepositoryException(Throwable cause) {
        super(cause);
    }
}
