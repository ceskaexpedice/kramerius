package cz.inovatika.kramerius.services.workers;

import cz.inovatika.kramerius.services.iterators.IterationItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorkerContext {

    protected List<WorkerIndexedItem> workerIndexedItems = new ArrayList<>();
    protected List<IterationItem> notIndexed= new ArrayList<>();

    public WorkerContext(List<WorkerIndexedItem> alreadyIndexed, List<IterationItem> notIndexed) {
        this.workerIndexedItems = alreadyIndexed;
        this.notIndexed = notIndexed;
    }

    public List<WorkerIndexedItem> getAlreadyIndexed() {
        return workerIndexedItems;
    }

    public Map<String, WorkerIndexedItem> getAlreadyIndexedAsMap() {
        return workerIndexedItems.stream()
                .collect(Collectors.toMap(WorkerIndexedItem::getId, r -> r));
    }

    public List<IterationItem> getNotIndexed() {
        return notIndexed;
    }
}
