package cz.incad.kramerius;

/** Too big exception */
public class TooBigException extends RuntimeException{

    private int counter;
    
    public TooBigException(String message, int counter) {
        super(message);
        this.counter = counter;
    }
    
    public int getCounter() {
        return counter;
    }
}
