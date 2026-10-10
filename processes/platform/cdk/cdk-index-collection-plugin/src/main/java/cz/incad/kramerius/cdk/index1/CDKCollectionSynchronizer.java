

package cz.incad.kramerius.cdk.index1;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CDKCollectionSynchronizer {

    private final SearchIndexService searchIndex;
    private final ProcessingIndexService processingIndex;

    public CDKCollectionSynchronizer(SearchIndexService searchIndex, ProcessingIndexService processingIndex) {
        this.searchIndex = searchIndex;
        this.processingIndex = processingIndex;
    }

    public void synchronize() {
        for (String cdkCollectionPid : processingIndex.getCDKCollections()) {
            List<String> cdkPath = getCDKPathFromRoot(cdkCollectionPid);
            for (String referencedPid : processingIndex.getCDKReferences(cdkCollectionPid)) {
                synchronizeReference(cdkCollectionPid, referencedPid, cdkPath);
            }
        }
    }

    private void synchronizeReference(String cdkCollectionPid, String referencedPid, List<String> cdkPath) {
        SearchDocument root = searchIndex.get(referencedPid);
        if (root == null) {
            return;
        }
        List<SearchDocument> subtree = searchIndex.findUnlinkedSubtree(referencedPid, cdkPath);
        for (SearchDocument document : subtree) {
            updateDocument(document, cdkCollectionPid, cdkPath, referencedPid);
        }
    }

    private List<String> getCDKPathFromRoot(String collectionPid) {
        Deque<String> path = new ArrayDeque<>();
        Set<String> visited = new LinkedHashSet<>();
        String currentPid = collectionPid;
        while (currentPid != null) {
            if (!visited.add(currentPid)) {
                throw new IllegalStateException("Cycle in CDK collection hierarchy at " + currentPid);
            }
            path.addFirst(currentPid);
            currentPid = processingIndex.getParentCollection(currentPid);
        }
        return new ArrayList<>(path);
    }

    private void updateDocument(SearchDocument document, String cdkCollectionPid, List<String> cdkPath, String referencedPid) {
        Set<String> inCollections = new LinkedHashSet<>(document.getInCollections());
        inCollections.addAll(cdkPath);

        String direct = document.getInCollectionsDirect();

        // Přímá reference se vztahuje jen na kořen reference.
        if (document.getPid().equals(referencedPid)) {
            if (direct != null && !direct.equals(cdkCollectionPid)) {
                throw new IllegalStateException("Multiple direct CDK memberships for " + document.getPid() + " are not supported by this version");
            }
            direct = cdkCollectionPid;
        }
        searchIndex.updateCollectionsFields(document.getPid(), new ArrayList<>(inCollections), direct);

        // Přidáme CDK cestu a zachováme ostatní cesty.
        String ownPidPath = document.getOwnPidPath();
        if (ownPidPath != null && !ownPidPath.isEmpty()) {
            String fullPath = String.join("/", cdkPath) + "/" + ownPidPath;
            Set<String> allPaths = new LinkedHashSet<>(document.getPidPaths());
            allPaths.add(fullPath);
            searchIndex.updatePidPaths(document.getPid(), new ArrayList<>(allPaths));
        }
    }
}
