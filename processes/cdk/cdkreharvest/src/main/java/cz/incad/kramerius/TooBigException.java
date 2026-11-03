package cz.incad.kramerius;

/** Too big exception */
public class TooBigException extends RuntimeException{

    private final int counter;
    private final int limit;
    
    public TooBigException(String message, int counter, int limit) {
        super(String.format("%s (found at least %d PID(s); configured limit is %d)", message, counter, limit));
        this.counter = counter;
        this.limit = limit;
    }
    
    public int getCounter() {
        return counter;
    }

    public int getLimit() {
        return limit;
    }
}
