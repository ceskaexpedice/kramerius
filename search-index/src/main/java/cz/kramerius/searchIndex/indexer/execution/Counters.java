package cz.kramerius.searchIndex.indexer.execution;

public class Counters {
    private int processed = 0;
    private int indexed = 0;
    private int ignored = 0;
    private int removed = 0;
    private int errors = 0;

    public void incrementProcessed() {
        processed += 1;
    }

    public void incrementIndexed() {
        indexed += 1;
    }

    public void incrementIgnored() {
        ignored += 1;
    }

    public void incrementRemoved() { removed += 1; }

    public void incrementErrors() {
        errors += 1;
    }

    public int getProcessed() {
        return processed;
    }

    public int getIndexed() {
        return indexed;
    }

    public int getIgnored() { return ignored; }

    public int getRemoved() { return removed; }

    public int getErrors() {
        return errors;
    }

}
