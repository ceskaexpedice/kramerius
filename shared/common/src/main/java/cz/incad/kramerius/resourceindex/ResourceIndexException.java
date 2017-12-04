package cz.incad.kramerius.resourceindex;

/**
 * Basic resource index exception
 * @author pstastny
 */
public class ResourceIndexException extends Exception {

    public ResourceIndexException() {
        super();
    }

    public ResourceIndexException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }

    public ResourceIndexException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public ResourceIndexException(String arg0) {
        super(arg0);
    }

    public ResourceIndexException(Throwable arg0) {
        super(arg0);
    }

}
