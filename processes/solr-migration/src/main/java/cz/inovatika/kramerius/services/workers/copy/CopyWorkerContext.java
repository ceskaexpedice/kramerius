package cz.inovatika.kramerius.services.workers.copy;

import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.workers.WorkerContext;
import cz.inovatika.kramerius.services.workers.WorkerIndexedItem;

import java.util.ArrayList;
import java.util.List;

public class CopyWorkerContext<T extends WorkerIndexedItem> extends WorkerContext {

    protected List<T> workerIndexedItems = new ArrayList<>();
    protected List<IterationItem> notIndexed= new ArrayList<>();

    public CopyWorkerContext(List<IterationItem> allItems, List<T> alreadyIndexed, List<IterationItem> notIndexed) {
        super(allItems);
        this.workerIndexedItems = alreadyIndexed;
        this.notIndexed = notIndexed;
    }


    public List<T> getAlreadyIndexed() {
        return workerIndexedItems;
    }


    public List<IterationItem> getNotIndexed() {
        return notIndexed;
    }
}
