package cz.inovatika.kramerius.services.workers.copy.simple;

import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.workers.WorkerIndexedItem;
import cz.inovatika.kramerius.services.workers.copy.CopyWorkerContext;

import java.util.List;

public class SimpleCopyWorkerContext extends CopyWorkerContext<WorkerIndexedItem> {

    public SimpleCopyWorkerContext(List<IterationItem> allItems, List<WorkerIndexedItem> alreadyIndexed, List<IterationItem> notIndexed) {
        super(allItems, alreadyIndexed, notIndexed);
    }
}
