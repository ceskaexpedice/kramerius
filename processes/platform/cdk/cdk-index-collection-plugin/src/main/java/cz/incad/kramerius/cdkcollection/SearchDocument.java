
package cz.incad.kramerius.cdkcollection;

import java.util.ArrayList;
import java.util.List;

public class SearchDocument {

    private final String pid;
    private final String ownPidPath;
    private List<String> inCollections;
    private List<String> inCollectionsDirect;
    private List<String> pidPaths;

    public boolean isCdkCollection() {
        return cdkCollection;
    }

    private boolean cdkCollection;

    public SearchDocument(String pid, String ownPidPath, List<String> inCollections,
                          List<String> inCollectionsDirect, List<String> pidPaths, boolean cdkCollection) {
        this.pid = pid;
        this.ownPidPath = ownPidPath;
        this.inCollections = copy(inCollections);
        this.inCollectionsDirect = copy(inCollectionsDirect);
        this.pidPaths = copy(pidPaths);
        this.cdkCollection = cdkCollection;
    }

    private static List<String> copy(List<String> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    public String getPid() {
        return pid;
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

    @Override
    public String toString() {
        return "SearchDocument{" +
                "pid='" + pid + '\'' +
                ", ownPidPath='" + ownPidPath + '\'' +
                ", inCollections=" + inCollections +
                ", inCollectionsDirect=" + inCollectionsDirect +
                ", pidPaths=" + pidPaths +
                ", cdkCollection=" + cdkCollection +
                '}';
    }
}
