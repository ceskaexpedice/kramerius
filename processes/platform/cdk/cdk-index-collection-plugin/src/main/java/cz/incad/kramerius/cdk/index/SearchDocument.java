package cz.incad.kramerius.cdk.index;

import java.util.ArrayList;
import java.util.List;

class SearchDocument {

    private final String pid;

    private List<String> inCollections = new ArrayList<>();

    private String inCollectionsDirect;

    public SearchDocument(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public List<String> getInCollections() {
        return inCollections;
    }

    public void setInCollections(List<String> values) {
        this.inCollections = new ArrayList<>(values);
    }

    public String getInCollectionsDirect() {
        return inCollectionsDirect;
    }

    public void setInCollectionsDirect(String value) {
        this.inCollectionsDirect = value;
    }

    @Override
    public String toString() {
        return pid;
    }
}