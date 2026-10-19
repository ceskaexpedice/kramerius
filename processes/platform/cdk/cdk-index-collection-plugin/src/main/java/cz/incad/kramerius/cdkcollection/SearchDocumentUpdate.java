package cz.incad.kramerius.cdkcollection;

import java.util.List;

public class SearchDocumentUpdate {

    private final String pid;
    // TODO foster_parent
    private final List<String> pidPaths;
    private final List<String> inCollections;
    private final List<String> inCollectionsDirect;

    public SearchDocumentUpdate(
            String pid,
            List<String> pidPaths,
            List<String> inCollections,
            List<String> inCollectionsDirect) {
        this.pid = pid;
        this.pidPaths = pidPaths;
        this.inCollections = inCollections;
        this.inCollectionsDirect = inCollectionsDirect;
    }

    public String getPid() {
        return pid;
    }

    public List<String> getPidPaths() {
        return pidPaths;
    }

    public List<String> getInCollections() {
        return inCollections;
    }

    public List<String> getInCollectionsDirect() {
        return inCollectionsDirect;
    }
}