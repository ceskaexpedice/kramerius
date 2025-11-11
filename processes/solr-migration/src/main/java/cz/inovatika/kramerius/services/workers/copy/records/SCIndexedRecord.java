package cz.inovatika.kramerius.services.workers.copy.records;

import java.util.Map;

public class SCIndexedRecord {

    private String id;
    private Map<String, Object> document;

    public SCIndexedRecord(String idField, Map<String,Object> document) {
        this.document = document;
        this.id = document.containsKey(idField) ? (String)document.get(idField) : null;
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getDocument() {
        return document;
    }
}
