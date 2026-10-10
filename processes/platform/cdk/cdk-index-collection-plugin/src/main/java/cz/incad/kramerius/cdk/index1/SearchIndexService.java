package cz.incad.kramerius.cdk.index1;

import java.util.List;

public interface SearchIndexService {
    SearchDocument get(String pid);

    /**
     * Returns all indexed documents whose own PID path
     * belongs to the subtree rooted at the given document.
     */
    List<SearchDocument> findUnlinkedSubtree(String rootPid, List<String> cdkPath);

    void updateCollectionsFields(
            String pid,
            List<String> inCollections,
            String inCollectionsDirect);

    void updatePidPaths(String pid, List<String> pidPaths);


}