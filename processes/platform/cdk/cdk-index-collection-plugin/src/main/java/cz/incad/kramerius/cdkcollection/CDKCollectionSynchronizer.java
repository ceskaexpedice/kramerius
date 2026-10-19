
package cz.incad.kramerius.cdkcollection;

import cz.incad.kramerius.plugin.CDKIndexCollection;

import java.util.*;
import java.util.logging.Logger;

public class CDKCollectionSynchronizer {
    private static final Logger LOGGER = Logger.getLogger(CDKIndexCollection.class.getName());

    private final SearchIndexService searchIndex;
    private final ProcessingIndexService processingIndex;

    public CDKCollectionSynchronizer(SearchIndexService searchIndex, ProcessingIndexService processingIndex) {
        this.searchIndex = searchIndex;
        this.processingIndex = processingIndex;
    }

    public void synchronize() {
        LOGGER.info("CDK Collection Synchronizer starts");
        List<ReferencePath> validReferences = buildValidReferences();
        // First remove obsolete paths and memberships.
        removeObsoleteReferences(validReferences);
        // Then preserve the existing addition algorithm.
        for (ReferencePath reference : validReferences) {
            synchronizeReference(reference);
        }
        LOGGER.info("CDK Collection Synchronizer finishes successfully");
    }

    private List<ReferencePath> buildValidReferences() {
        List<ReferencePath> result = new ArrayList<>();
        List<String> cdkCollections = processingIndex.getCDKCollections();
        for (String collectionPid : cdkCollections) {
            List<String> cdkPath = getCDKPathFromRoot(collectionPid);
            List<String> cdkReferences = processingIndex.getCDKReferences(collectionPid);
            for (String referencedPid : cdkReferences) {
                SearchDocument root = searchIndex.get(referencedPid);
                if (root == null || root.getOwnPidPath() == null || root.getOwnPidPath().isEmpty()) {
                    continue;
                }
                result.add(new ReferencePath(collectionPid, referencedPid, cdkPath,
                        String.join("/", cdkPath) + "/" + referencedPid));
            }
        }
        return result;
    }

    private void removeObsoleteReferences(List<ReferencePath> validReferences) {
        Map<String, List<ReferencePath>> referencesByRoot = new LinkedHashMap<>();
        for (ReferencePath reference : validReferences) {
            referencesByRoot.computeIfAbsent(reference.referencedPid, key -> new ArrayList<>()).add(reference);
        }
        Map<String, SearchDocument> staleRoots = new LinkedHashMap<>();
        List<String> directCollectionValues = searchIndex.getDirectCollectionValues();
        for (String directCollection : directCollectionValues) {
            SearchDocument collection = searchIndex.get(directCollection);
            if (collection != null && !collection.isCdkCollection()) {
                // Běžná zdrojová knihovní kolekce.
                continue;
            }
            searchIndex.findByDirectCollection(directCollection, roots -> {
                for (SearchDocument root : roots) {
                    if (root.isCdkCollection()) {
                        continue;
                    }
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
                        // obsoletePrefixes = new HashSet<>();
                    }
                    for (String stalePrefix : obsoletePrefixes) {
                        staleRoots.putIfAbsent(stalePrefix, root);
                    }
                }
            });
        }
        for (Map.Entry<String, SearchDocument> entry : staleRoots.entrySet()) {
            removePathPrefix(entry.getKey(), entry.getValue(), validReferences);
        }
    }

    private Set<String> findObsoletePrefixes(SearchDocument root, String removedCollection, List<ReferencePath> expectedReferences) {
        Set<String> obsolete = new LinkedHashSet<>();
        String ownPath = root.getOwnPidPath();
        if (ownPath == null || ownPath.isEmpty()) {
            return obsolete;
        }
        String suffix = "/" + root.getPid();
        for (String path : root.getPidPaths()) {
            if (path == null || !path.endsWith(suffix)) {
                continue;
            }
            String prefix = path.substring(0, path.length() - suffix.length());
            if (prefix.isEmpty()) {
                continue;
            }
            boolean valid = false;
            for (ReferencePath reference : expectedReferences) {
                if (reference.fullPath.equals(path)) {
                    valid = true;
                    break;
                }
            }
            if (valid) {
                continue;
            }
            String lastPathSegment = lastPathSegment(prefix);
            if (removedCollection != null) {
                if (lastPathSegment.equals(removedCollection)) {
                    obsolete.add(path);
                }
            } else if (processingIndex.isCDKCollection(lastPathSegment)) {
                obsolete.add(path);
            }
        }
        return obsolete;
    }

    private String lastPathSegment(String path) {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }

    private void removePathPrefix(String staleRootPath, SearchDocument root, List<ReferencePath> validReferences) {
        List<SearchDocumentUpdate> updates = new ArrayList<>();
        String rootSuffix = "/" + root.getPid();
        if (!staleRootPath.endsWith(rootSuffix)) {
            throw new IllegalArgumentException("Stale root path does not end with root own PID path: " + staleRootPath);
        }
        String removedCdkPrefix = staleRootPath.substring(0, staleRootPath.length() - rootSuffix.length());
        searchIndex.findByPidPathPrefix(
                staleRootPath,
                page -> {
                    for (SearchDocument document : page) {
                        LOGGER.info(String.format("Removing obsolete reference \"%s\" in pid \"%s\"", staleRootPath, document.getPid()));
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
                        updates.add(new SearchDocumentUpdate(
                                document.getPid(),
                                remainingPaths,
                                new ArrayList<>(inCollections),
                                direct));
                    }
                    if (!updates.isEmpty()) {
                        searchIndex.updateDocumentFieldsBatch(updates);
                    }
                });

    }

    private void synchronizeReference(ReferencePath reference) {
        searchIndex.findUnlinkedSubtree(reference.referencedPid, reference.cdkPath,
                page -> {
                    List<SearchDocumentUpdate> updates = new ArrayList<>();
                    for (SearchDocument document : page) {
                        SearchDocumentUpdate update = createDocumentUpdate(document, reference);
                        if (update != null) {
                            updates.add(update);
                        }
                    }
                    if (!updates.isEmpty()) {
                        searchIndex.updateDocumentFieldsBatch(updates);
                    }
                });
    }

    private SearchDocumentUpdate createDocumentUpdate(SearchDocument document, ReferencePath reference) {
        Set<String> inCollections = new LinkedHashSet<>(document.getInCollections());
        inCollections.addAll(reference.cdkPath);
        List<String> direct = new ArrayList<>(document.getInCollectionsDirect());
        if (document.getPid().equals(reference.referencedPid) && !direct.contains(reference.collectionPid)) {
            direct.add(reference.collectionPid);
        }
        List<String> pidPaths = document.getPidPaths();
        Set<String> allPaths = new LinkedHashSet<>(pidPaths);
        String ownPidPath = document.getOwnPidPath();
        if (ownPidPath != null && !ownPidPath.isEmpty()) {
            int index = ownPidPath.indexOf(reference.referencedPid);
            if (index >= 0) {
                String lastPath = ownPidPath.substring(index);
                String fullPath = String.join("/", reference.cdkPath) + "/" + lastPath;
                allPaths.add(fullPath);
            }
        }
        return new SearchDocumentUpdate(
                document.getPid(),
                new ArrayList<>(allPaths),
                new ArrayList<>(inCollections),
                direct);
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

        @Override
        public String toString() {
            return "ReferencePath{" +
                    "collectionPid='" + collectionPid + '\'' +
                    ", referencedPid='" + referencedPid + '\'' +
                    ", cdkPath=" + cdkPath +
                    ", fullPath='" + fullPath + '\'' +
                    '}';
        }
    }
}
