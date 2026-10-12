
package cz.incad.kramerius.cdk.index1;

import java.util.*;

public class CDKCollectionSynchronizer {

    private final SearchIndexService searchIndex;
    private final ProcessingIndexService processingIndex;

    public CDKCollectionSynchronizer(SearchIndexService searchIndex, ProcessingIndexService processingIndex) {
        this.searchIndex = searchIndex;
        this.processingIndex = processingIndex;
    }

    public void synchronize() {
        List<ReferencePath> validReferences = buildValidReferences();
        // First remove obsolete paths and memberships.
        removeObsoleteReferences(validReferences);
        // Then preserve the existing addition algorithm.
        for (ReferencePath reference : validReferences) {
            synchronizeReference(reference);
        }
    }

    private List<ReferencePath> buildValidReferences() {
        List<ReferencePath> result = new ArrayList<>();
        for (String collectionPid : processingIndex.getCDKCollections()) {
            List<String> cdkPath = getCDKPathFromRoot(collectionPid);
            for (String referencedPid : processingIndex.getCDKReferences(collectionPid)) {
                SearchDocument root = searchIndex.get(referencedPid);
                if (root == null || root.getOwnPidPath() == null || root.getOwnPidPath().isEmpty()) {
                    continue;
                }
                result.add(new ReferencePath(collectionPid, referencedPid, cdkPath,
                        String.join("/", cdkPath) + "/" + root.getOwnPidPath()));
            }
        }
        return result;
    }

    private void removeObsoleteReferences(List<ReferencePath> validReferences) {
        Map<String, List<ReferencePath>> referencesByRoot = new LinkedHashMap<>();
        for (ReferencePath reference : validReferences) {
            referencesByRoot.computeIfAbsent(reference.referencedPid, key -> new ArrayList<>()).add(reference);
        }
        // Prefix -> kořenový dokument, ze kterého prefix pochází.
        Map<String, SearchDocument> staleRoots = new LinkedHashMap<>();
        List<String> directCollectionValues = searchIndex.getDirectCollectionValues();
        for (String directCollection : directCollectionValues) {
            List<SearchDocument> roots = searchIndex.findByDirectCollection(directCollection);
            for (SearchDocument root : roots) {
                // TODO !!!!!!!!!!!!!!
//            SearchDocument collection = searchIndex.get(directCollection);
//            if (collection != null && collection.getSource() != null) { // cdk.collections
//                // Běžná zdrojová knihovní kolekce.
//                continue;
//            }
                List<ReferencePath> expectedReferences = referencesByRoot.getOrDefault(root.getPid(), Collections.emptyList());
                boolean directMembershipIsValid = false;
                for (ReferencePath reference : expectedReferences) {
                    if (reference.collectionPid.equals(directCollection)) {
                        directMembershipIsValid = true;
                        break;
                    }
                }
                Set<String> obsoletePrefixes;
                if (!directMembershipIsValid) {
                    // Přímá reference byla odstraněna z processing indexu.
                    obsoletePrefixes = findObsoletePrefixes(root, directCollection, expectedReferences);
                } else {
                    // Reference stále existuje, ale hierarchie se mohla změnit.
                    obsoletePrefixes = findObsoletePrefixes(root, null, expectedReferences);
                }
                for (String stalePrefix : obsoletePrefixes) {
                    staleRoots.putIfAbsent(stalePrefix, root);
                }
            }
        }
        for (Map.Entry<String, SearchDocument> entry : staleRoots.entrySet()) {
            removePathPrefix(entry.getKey(), entry.getValue(), validReferences);
        }
    }

    private Set<String> findObsoletePrefixes(SearchDocument root, String removedCollection, List<ReferencePath> expectedReferences) {
        Set<String> validPrefixes = new LinkedHashSet<>();
        for (ReferencePath reference : expectedReferences) {
            validPrefixes.add(reference.fullPath);
        }
        Set<String> obsolete = new LinkedHashSet<>();
        String ownPath = root.getOwnPidPath();
        if (ownPath == null || ownPath.isEmpty()) {
            return obsolete;
        }
        String suffix = "/" + ownPath;
        for (String path : root.getPidPaths()) {
            if (path == null || !path.endsWith(suffix)) {
                continue;
            }
            String prefix = path.substring(0, path.length() - suffix.length());
            if (prefix.isEmpty()) {
                continue;
            }
            String fullRootPath = prefix + suffix;
            boolean valid = false;
            for (ReferencePath reference : expectedReferences) {
                if (reference.fullPath.equals(fullRootPath)) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                if (removedCollection == null || lastPathSegment(prefix).equals(removedCollection)) {
                    obsolete.add(fullRootPath);
                }
            }
        }
        return obsolete;
    }

    private String lastPathSegment(String path) {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }

    private void removePathPrefix(String staleRootPath, SearchDocument root, List<ReferencePath> validReferences) {
        String rootOwnPidPath = root.getOwnPidPath();
        String rootSuffix = "/" + rootOwnPidPath;
        if (!staleRootPath.endsWith(rootSuffix)) {
            throw new IllegalArgumentException("Stale root path does not end with root own PID path: " + staleRootPath);
        }
        // Prefix vypočítáme podle kořene, ne podle aktuálního dokumentu.
        String removedCdkPrefix = staleRootPath.substring(0, staleRootPath.length() - rootSuffix.length());
        List<SearchDocument> affected = searchIndex.findByPidPathPrefix(staleRootPath);
        for (SearchDocument document : affected) {
            List<String> currentPaths = document.getPidPaths();
            List<String> remainingPaths = new ArrayList<>();
            for (String path : currentPaths) {
                if (path == null || !(path.equals(staleRootPath) || path.startsWith(staleRootPath + "/"))) {
                    remainingPaths.add(path);
                }
            }
            if (remainingPaths.size() == currentPaths.size()) {
                continue;
            }
            Set<String> validMemberships = new LinkedHashSet<>();
            for (ReferencePath reference : validReferences) {
                String prefix = String.join("/", reference.cdkPath) + "/";
                for (String path : remainingPaths) {
                    if (path != null && path.startsWith(prefix)) {
                        validMemberships.addAll(reference.cdkPath);
                        break;
                    }
                }
            }
            Set<String> inCollections = new LinkedHashSet<>(document.getInCollections());
            for (String collectionPid : removedCdkPrefix.split("/")) {
                if (!validMemberships.contains(collectionPid)) {
                    inCollections.remove(collectionPid);
                }
            }
            List<String> direct = new ArrayList<>(document.getInCollectionsDirect());
            Iterator<String> iterator = direct.iterator();
            while (iterator.hasNext()) {
                String collectionPid = iterator.next();
                if (!processingIndex.isCDKCollection(collectionPid) ||
                        !processingIndex.getCDKReferences(collectionPid).contains(document.getPid())) {
                    iterator.remove();
                }
            }
            searchIndex.updatePidPaths(document.getPid(), remainingPaths);
            searchIndex.updateCollectionsFields(document.getPid(), new ArrayList<>(inCollections), direct);
        }
    }

    private void synchronizeReference(ReferencePath reference) {
        List<SearchDocument> subtree = searchIndex.findUnlinkedSubtree(reference.referencedPid, reference.cdkPath);
        for (SearchDocument document : subtree) {
            updateDocument(document, reference);
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

    private void updateDocument(SearchDocument document, ReferencePath reference) {
        Set<String> inCollections = new LinkedHashSet<>(document.getInCollections());
        inCollections.addAll(reference.cdkPath);
        List<String> direct = new ArrayList<>(document.getInCollectionsDirect());
        if (document.getPid().equals(reference.referencedPid) && !direct.contains(reference.collectionPid)) {
            direct.add(reference.collectionPid);
        }
        searchIndex.updateCollectionsFields(document.getPid(), new ArrayList<>(inCollections), direct);
        String ownPidPath = document.getOwnPidPath();
        if (ownPidPath != null && !ownPidPath.isEmpty()) {
            String fullPath = String.join("/", reference.cdkPath) + "/" + ownPidPath;
            Set<String> allPaths = new LinkedHashSet<>(document.getPidPaths());
            allPaths.add(fullPath);
            searchIndex.updatePidPaths(document.getPid(), new ArrayList<>(allPaths));
        }
    }

    private static class ReferencePath {
        private final String collectionPid;
        private final String referencedPid;
        private final List<String> cdkPath;
        private final String fullPath;

        private ReferencePath(String collectionPid, String referencedPid, List<String> cdkPath, String fullPath) {
            this.collectionPid = collectionPid;
            this.referencedPid = referencedPid;
            this.cdkPath = new ArrayList<>(cdkPath);
            this.fullPath = fullPath;
        }
    }
}
