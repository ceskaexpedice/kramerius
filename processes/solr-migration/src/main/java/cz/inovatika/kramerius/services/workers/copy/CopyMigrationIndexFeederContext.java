package cz.inovatika.kramerius.services.workers.copy;

import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.workers.MigrationIndexFeederContext;
import cz.inovatika.kramerius.services.workers.WorkerIndexedItem;

import java.util.List;

public class CopyMigrationIndexFeederContext<T extends WorkerIndexedItem> extends MigrationIndexFeederContext {

    protected List<T> workerIndexedItems;
    protected List<IterationItem> notIndexed;

    public CopyMigrationIndexFeederContext(List<IterationItem> allItems, List<T> alreadyIndexed, List<IterationItem> notIndexed) {
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
