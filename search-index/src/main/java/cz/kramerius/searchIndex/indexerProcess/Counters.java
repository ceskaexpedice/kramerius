package cz.kramerius.searchIndex.indexerProcess;

public class Counters {
    private int found = 0;
    private int indexed = 0;
    private int removed = 0;
    private int errors = 0;

    public void incrementFound() {
        found += 1;
    }

    public void incrementIndexed() {
        indexed += 1;
    }

    public void incrementRemoved() {
        removed += 1;
    }

    public void incrementErrors() {
        errors += 1;
    }

    public int getFound() {
        return found;
    }

    public int getIndexed() {
        return indexed;
    }

    public int getRemoved() {
        return removed;
    }

    public int getErrors() {
        return errors;
    }

    public int getProcessed() {
        return indexed + removed + errors;
    }
}
