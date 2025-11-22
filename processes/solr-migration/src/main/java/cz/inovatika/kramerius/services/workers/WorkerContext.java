package cz.inovatika.kramerius.services.workers;

import cz.inovatika.kramerius.services.iterators.IterationItem;

import java.util.List;

public abstract class WorkerContext {

    protected List<IterationItem> batchItems;

    public WorkerContext(List<IterationItem> allItems) {
        this.batchItems = allItems;
    }

    public List<IterationItem> getBatchItems() {
        return batchItems;
    }
}
