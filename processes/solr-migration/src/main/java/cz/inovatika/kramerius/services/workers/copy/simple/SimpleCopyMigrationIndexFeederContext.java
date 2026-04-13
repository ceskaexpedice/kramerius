package cz.inovatika.kramerius.services.workers.copy.simple;

import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.workers.WorkerIndexedItem;
import cz.inovatika.kramerius.services.workers.copy.CopyMigrationIndexFeederContext;

import java.util.List;

public class SimpleCopyMigrationIndexFeederContext extends CopyMigrationIndexFeederContext<WorkerIndexedItem> {

    public SimpleCopyMigrationIndexFeederContext(List<IterationItem> allItems, List<WorkerIndexedItem> alreadyIndexed, List<IterationItem> notIndexed) {
        super(allItems, alreadyIndexed, notIndexed);
    }
}
