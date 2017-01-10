package cz.incad.kramerius.virtualcollections;

public class CollectionException extends Exception{

    public CollectionException() {
        super();
    }

    public CollectionException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }

    public CollectionException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public CollectionException(String arg0) {
        super(arg0);
    }

    public CollectionException(Throwable arg0) {
        super(arg0);
    }
}
