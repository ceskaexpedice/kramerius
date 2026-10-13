
package cz.incad.kramerius.cdkcollection;

import java.util.List;

public interface SearchIndexService {

    SearchDocument get(String pid);

    /**
     * Finds documents in the source subtree that do not yet
     * contain the target CDK path.
     */
    List<SearchDocument> findUnlinkedSubtree(String rootPid, List<String> cdkPath);

    /**
     * Finds documents whose in_collections.direct field contains
     * the given CDK collection PID.
     * <p>
     * Normally these are the roots of direct CDK references.
     */
    List<SearchDocument> findByDirectCollection(String collectionPid);

    /**
     * Finds documents whose pid_paths contains the given path
     * or a descendant path.
     */
    List<SearchDocument> findByPidPathPrefix(String pidPathPrefix);

    void updateCollectionsFields(String pid, List<String> inCollections, List<String> inCollectionsDirect);

    void updatePidPaths(String pid, List<String> pidPaths);

    /**
     * Returns all distinct values currently present in
     * in_collections.direct.
     */
    List<String> getDirectCollectionValues();
}
