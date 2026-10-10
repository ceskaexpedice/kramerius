package cz.incad.kramerius.cdk.index1;

import java.util.ArrayList;
import java.util.List;

public class SearchDocument {
    private final String pid;
    private final String ownPidPath;
    private List<String> inCollections;
    private String inCollectionsDirect;
    private List<String> pidPaths;

    public SearchDocument(
            String pid,
            String ownPidPath,
            List<String> inCollections,
            String inCollectionsDirect,
            List<String> pidPaths) {
        this.pid = pid;
        this.ownPidPath = ownPidPath;
        this.inCollections = copy(inCollections);
        this.inCollectionsDirect = inCollectionsDirect;
        this.pidPaths = copy(pidPaths);
    }

    private static List<String> copy(List<String> values) {
        return values == null
                ? new ArrayList<>()
                : new ArrayList<>(values);
    }

    public String getPid() {
        return pid;
    }

    public String getOwnPidPath() {
        return ownPidPath;
    }

    public List<String> getInCollections() {
        return new ArrayList<>(inCollections);
    }

    public String getInCollectionsDirect() {
        return inCollectionsDirect;
    }

    public List<String> getPidPaths() {
        return new ArrayList<>(pidPaths);
    }

    public void setCollections(
            List<String> inCollections,
            String inCollectionsDirect) {
        this.inCollections = copy(inCollections);
        this.inCollectionsDirect = inCollectionsDirect;
    }

    public void setPidPaths(List<String> pidPaths) {
        this.pidPaths = copy(pidPaths);
    }
}