package cz.inovatika.kramerius.services.workers.copy;

import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.workers.copy.records.SCIndexedRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleCopyContext {

    private List<SCIndexedRecord> indexedRecords = new ArrayList<>();
    private List<IterationItem> notIndexed= new ArrayList<>();

    public SimpleCopyContext(List<SCIndexedRecord> alreadyIndexed,  List<IterationItem> notIndexed) {
        this.notIndexed = notIndexed;
        this.indexedRecords = alreadyIndexed;
    }

    public List<SCIndexedRecord> getAlreadyIndexed() {
        return indexedRecords;
    }

    public Map<String, SCIndexedRecord> getAlreadyIndexedAsMap() {
        return indexedRecords.stream()
                .collect(Collectors.toMap(SCIndexedRecord::getId, r -> r));
    }

    public List<IterationItem> getNotIndexed() {
        return notIndexed;
    }


}
