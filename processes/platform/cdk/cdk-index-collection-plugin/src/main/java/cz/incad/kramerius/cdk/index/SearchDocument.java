package cz.incad.kramerius.cdk.index;

import java.util.ArrayList;
import java.util.List;

public class SearchDocument {
    private final String pid;
    private List<String> inCollections;
    private String inCollectionsDirect;

    public SearchDocument(String pid, List<String> inCollections, String inCollectionsDirect) {
        this.pid = pid;
        this.inCollections = inCollections == null ? new ArrayList<>() : new ArrayList<>(inCollections);
        this.inCollectionsDirect = inCollectionsDirect;
    }

    public String getPid() {
        return pid;
    }

    public List<String> getInCollections() {
        return inCollections;
    }

    public void setInCollections(List<String> inCollections) {
        this.inCollections = inCollections == null ? new ArrayList<>() : new ArrayList<>(inCollections);
    }

    public String getInCollectionsDirect() {
        return inCollectionsDirect;
    }

    public void setInCollectionsDirect(String inCollectionsDirect) {
        this.inCollectionsDirect = inCollectionsDirect;
    }

    @Override
    public String toString() {
        return "SearchDocument{" +
                "pid='" + pid + '\'' +
                ", inCollections=" + inCollections +
                ", inCollectionsDirect='" + inCollectionsDirect + '\'' +
                '}';
    }
}