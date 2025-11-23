package cz.inovatika.kramerius.services.workers;

import java.util.Map;

public class WorkerIndexedItem {

    private final String id;
    private final Map<String, Object> document;

    public WorkerIndexedItem(String id, Map<String,Object> document) {
        this.document = document;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getDocument() {
        return document;
    }
}
