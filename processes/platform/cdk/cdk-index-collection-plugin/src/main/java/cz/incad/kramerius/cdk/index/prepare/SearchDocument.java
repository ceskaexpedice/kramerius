package cz.incad.kramerius.cdk.index.prepare;

import java.util.ArrayList;
import java.util.List;

class SearchDocument {

    private final String pid;

    private List<String> inCollection =
            new ArrayList<String>();

    private String inCollectionDirect;

    public SearchDocument(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public List<String> getInCollection() {
        return inCollection;
    }

    public void setInCollection(
            List<String> values) {

        this.inCollection =
                new ArrayList<String>(values);
    }

    public String getInCollectionDirect() {
        return inCollectionDirect;
    }

    public void setInCollectionDirect(
            String value) {

        this.inCollectionDirect = value;
    }

    public Object getField(String name) {

        if ("in-collection".equals(name)) {
            return inCollection;
        }

        if ("in-collection-direct".equals(name)) {
            return inCollectionDirect;
        }

        throw new IllegalArgumentException(
                "Unknown field: " + name);
    }

    public List<String> getMultiValueField(String name) {

        if ("in-collection".equals(name)) {
            return inCollection;
        }

        throw new IllegalArgumentException(
                "Unknown multi-value field: " + name);
    }

    @SuppressWarnings("unchecked")
    public void setField(
            String name,
            Object value) {

        if ("in-collection".equals(name)) {
            setInCollection(
                    (List<String>) value);
            return;
        }

        if ("in-collection-direct".equals(name)) {
            setInCollectionDirect(
                    (String) value);
            return;
        }

        throw new IllegalArgumentException(
                "Unknown field: " + name);
    }
}