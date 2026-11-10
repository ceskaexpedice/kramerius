
package cz.incad.kramerius.cdkcollections;

import java.util.ArrayList;
import java.util.List;

public class SearchDocument {

    private final String compositeId;
    private final String pid;
    private final String ownPidPath;
    private List<String> inCollections;
    private List<String> inCollectionsDirect;
    private List<String> pidPaths;
    private boolean cdkCollection;
    private final String model;


    public SearchDocument(String compositeId, String pid, String ownPidPath, List<String> inCollections,
                          List<String> inCollectionsDirect, List<String> pidPaths, String model, boolean cdkCollection) {
        this.compositeId = compositeId;
        this.pid = pid;
        this.ownPidPath = ownPidPath;
        this.inCollections = copy(inCollections);
        this.inCollectionsDirect = copy(inCollectionsDirect);
        this.pidPaths = copy(pidPaths);
        this.cdkCollection = cdkCollection;
        this.model = model;
    }

    private static List<String> copy(List<String> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    public String getPid() {
        return pid;
    }

    public String getCompositeId() {
        return compositeId;
    }

    public String getOwnPidPath() {
        return ownPidPath;
    }

    public List<String> getInCollections() {
        return copy(inCollections);
    }

    public List<String> getInCollectionsDirect() {
        return copy(inCollectionsDirect);
    }

    public List<String> getPidPaths() {
        return copy(pidPaths);
    }

    public void setCollections(List<String> inCollections, List<String> inCollectionsDirect) {
        this.inCollections = copy(inCollections);
        this.inCollectionsDirect = copy(inCollectionsDirect);
    }

    public void setPidPaths(List<String> pidPaths) {
        this.pidPaths = copy(pidPaths);
    }

    public boolean isCdkCollection() {
        return cdkCollection;
    }

    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        return "SearchDocument{" +
                "pid='" + pid + '\'' +
                ", compositeId='" + compositeId + '\'' +
                ", ownPidPath='" + ownPidPath + '\'' +
                ", inCollections=" + inCollections +
                ", inCollectionsDirect=" + inCollectionsDirect +
                ", pidPaths=" + pidPaths +
                ", cdkCollection=" + cdkCollection +
                '}';
    }
}
