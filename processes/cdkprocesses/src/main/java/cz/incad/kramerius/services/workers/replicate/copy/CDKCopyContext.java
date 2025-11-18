package cz.incad.kramerius.services.workers.replicate.copy;

import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.workers.WorkerContext;
import cz.inovatika.kramerius.services.workers.WorkerIndexedItem;

import java.util.List;

public class CDKCopyContext extends WorkerContext {

    public CDKCopyContext(List<WorkerIndexedItem> alreadyIndexed, List<IterationItem> notIndexed) {
        super(alreadyIndexed, notIndexed);
    }
}
