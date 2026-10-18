
package cz.incad.kramerius.cdkcollection;

import java.util.List;
import java.util.function.Consumer;

public interface SearchIndexService {

    SearchDocument get(String pid);

    /**
     * Finds documents in the source subtree that do not yet
     * contain the target CDK path.
     */
    void findUnlinkedSubtree(String rootPid, List<String> cdkPath, Consumer<List<SearchDocument>> pageConsumer);

    /**
     * Finds documents whose in_collections.direct field contains
     * the given CDK collection PID.
     * <p>
     * Normally these are the roots of direct CDK references.
     */
    void findByDirectCollection(String collectionPid, Consumer<List<SearchDocument>> pageConsumer);

    /**
     * Finds documents whose pid_paths contains the given path
     * or a descendant path.
     */
    void findByPidPathPrefix(String pidPathPrefix, Consumer<List<SearchDocument>> pageConsumer);

    /**
     * Returns all distinct values currently present in
     * in_collections.direct.
     */
    List<String> getDirectCollectionValues();

    void updateCollectionsFields(String pid, List<String> inCollections, List<String> inCollectionsDirect);

    void updatePidPaths(String pid, List<String> pidPaths);

}
