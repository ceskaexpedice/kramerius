package cz.incad.kramerius.cdkcollections;

import org.ceskaexpedice.akubra.reference.CDKReference;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CDKCollectionSynchronizer {
    private static final Logger LOGGER = Logger.getLogger(CDKCollectionSynchronizer.class.getName());

    private final SearchIndexService searchIndex;
    private final ProcessingIndexService processingIndex;

    public CDKCollectionSynchronizer(SearchIndexService searchIndex, ProcessingIndexService processingIndex) {
        this.searchIndex = searchIndex;
        this.processingIndex = processingIndex;
    }

    public void synchronize() {
        LOGGER.info("CDK Collection Synchronizer starts");
        List<ReferencePath> validReferences = buildValidReferences();
        if (validReferences.isEmpty()) {
            LOGGER.warning("No CDK collections references found");
        } else {
            LOGGER.info(String.format("CDK collections references:\n%s",
                    validReferences.stream()
                            .map(ReferencePath::toString)
                            .collect(Collectors.joining("\n"))));
        }
        // First remove obsolete paths and memberships.
        removeObsoleteReferences(validReferences);
        // Then preserve the existing addition algorithm.
        for (ReferencePath reference : validReferences) {
            addCDKCollectionReference(reference);
            searchIndex.commit();
        }
        searchIndex.commit();
        LOGGER.info("CDK Collection Synchronizer finished successfully");
    }

    private List<ReferencePath> buildValidReferences() {
        List<ReferencePath> result = new ArrayList<>();
        for (String collectionPid : processingIndex.getCollections()) {
            List<List<String>> cdkPaths = getCDKPathsFromRoots(collectionPid);
            List<String> cdkReferences = processingIndex.getReferences(collectionPid);
            for (String referencedPid : cdkReferences) {
                String sourcePid = removeCdkPrefix(referencedPid);
                SearchDocument root = searchIndex.get(sourcePid);
                if (root == null || root.getOwnPidPath() == null || root.getOwnPidPath().isEmpty()) {
                    continue;
                }
                String ownPidPath = root.getOwnPidPath();
                int index = ownPidPath.indexOf(sourcePid);
                if (index < 0) {
                    continue;
                }
                String referencedPath = ownPidPath.substring(index);
                for (List<String> cdkPath : cdkPaths) {
                    String fullPath = String.join("/", cdkPath) + "/" + referencedPath;
                    result.add(new ReferencePath(
                            collectionPid,
                            sourcePid,
                            cdkPath,
                            fullPath));
                }
            }
        }
        return result;
    }

    private void removeObsoleteReferences(List<ReferencePath> validReferences) {
        Map<String, List<ReferencePath>> referencesByRoot = new LinkedHashMap<>();
        for (ReferencePath reference : validReferences) {
            referencesByRoot.computeIfAbsent(reference.getReferencedPid(), key -> new ArrayList<>()).add(reference);
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
                        if (reference.getCollectionPid().equals(directCollection)) {
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
            removeCDKCollectionReference(entry.getKey(), entry.getValue(), validReferences);
        }
    }

    private Set<String> findObsoletePrefixes(SearchDocument root, String removedCollection, List<ReferencePath> expectedReferences) {
        Set<String> obsolete = new LinkedHashSet<>();
        String suffix = "/" + root.getPid();
        for (String path : root.getPidPaths()) {
            if (path == null || !path.endsWith(suffix)) {
                continue;
            }
            String prefix = path.substring(0, path.length() - suffix.length());
            if (prefix.isEmpty()) {
                continue;
            }
            String lastPathSegment = lastPathSegment(prefix);
            if (removedCollection != null) {
                if (lastPathSegment.equals(removedCollection)) {
                    obsolete.add(path);
                }
                continue;
            }
            SearchDocument searchDocument = searchIndex.get(lastPathSegment);
            if (!searchDocument.isCdkCollection()) {
                continue;
            }
            boolean valid = expectedReferences.stream().anyMatch(reference -> reference.getFullPath().equals(path));
            if (!valid) {
                obsolete.add(path);
            }
        }
        return obsolete;
    }

    private String lastPathSegment(String path) {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }

    private void removeCDKCollectionReference(String staleRootPath, SearchDocument root, List<ReferencePath> validReferences) {
        List<SearchDocumentUpdate> updates = new ArrayList<>();
        String rootSuffix = "/" + root.getPid();
        if (!staleRootPath.endsWith(rootSuffix)) {
            throw new IllegalArgumentException("Stale root path does not end with root own PID path: " + staleRootPath);
        }
        String removedCdkPrefix = staleRootPath.substring(0, staleRootPath.length() - rootSuffix.length());
        final int[] updatesCounter = {0};
        searchIndex.findByPidPathPrefix(
                staleRootPath,
                page -> {
                    for (SearchDocument document : page) {
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
                            String prefix = String.join("/", reference.getCdkPath()) + "/";
                            for (String path : remainingPaths) {
                                if (path != null && path.startsWith(prefix)) {
                                    validMemberships.addAll(reference.getCdkPath());
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
                            SearchDocument searchDocument = searchIndex.get(collectionPid);
                            if (searchDocument != null &&  !searchDocument.isCdkCollection()) {
                                continue;
                            }
                            if (!processingIndex.getReferences(collectionPid).contains(new CDKReference(document.getPid()).getPrefixedPid())) {
                                iterator.remove();
                            }
                        }
                        updates.add(new SearchDocumentUpdate(
                                document.getCompositeId(),
                                document.getPid(),
                                remainingPaths,
                                new ArrayList<>(inCollections),
                                direct));
                        updatesCounter[0]++;
                    }
                    if (!updates.isEmpty()) {
                        searchIndex.updateDocumentFieldsBatch(updates);
                    }
                });
        if (updatesCounter[0] > 0) {
            LOGGER.info(String.format("Remove CDK collection reference %s from the %s subtree. Number of updates: %d",
                    staleRootPath, root.getPid(), updatesCounter[0]));
        }
        searchIndex.commit();
    }

    private void addCDKCollectionReference(ReferencePath reference) {
        final int[] updatesCounter = {0};
        searchIndex.findUnlinkedSubtree(reference/*reference.referencedPid, reference.cdkPath*/,
                page -> {
                    List<SearchDocumentUpdate> updates = new ArrayList<>();
                    for (SearchDocument document : page) {
                        SearchDocumentUpdate update = createDocumentUpdate(document, reference);
                        if (update != null) {
                            updates.add(update);
                            updatesCounter[0]++;
                        }
                    }
                    if (!updates.isEmpty()) {
                        searchIndex.updateDocumentFieldsBatch(updates);
                    }
                });
        if (updatesCounter[0] > 0) {
            LOGGER.info(String.format("Add CDK collection reference %s to documents in the %s subtree. Number of updates: %d",
                    reference.getFullPath(), reference.getReferencedPid(), updatesCounter[0]));
        }
    }

    private SearchDocumentUpdate createDocumentUpdate(SearchDocument document, ReferencePath reference) {
        Set<String> inCollections = new LinkedHashSet<>(document.getInCollections());
        inCollections.addAll(reference.getCdkPath());
        List<String> direct = new ArrayList<>(document.getInCollectionsDirect());
        if (document.getPid().equals(reference.getReferencedPid()) && !direct.contains(reference.getCollectionPid())) {
            direct.add(reference.getCollectionPid());
        }
        List<String> pidPaths = document.getPidPaths();
        Set<String> allPaths = new LinkedHashSet<>(pidPaths);
        // pokud je to nevlastni dite, pak se nemuze resit own pid path ale to jak je zarazeny ve sbirce ?
        // reference pid na urci cestu v pid_paths, kterou mame urcit
        // own_pid_path nam urci, jak hluboko jsme nyni zanoreni ... vytisk, stranka atd..
        // nebude stacit pid_paths dane sbirky?  Najit pid_path ktere se to tyka a pak pouzivat stejny orez ?
        String ownPidPath = document.getOwnPidPath();

        if (ownPidPath != null && !ownPidPath.isEmpty()) {
            int index = ownPidPath.indexOf(reference.getReferencedPid());
            if (index >= 0) {
                String lastPath = ownPidPath.substring(index);
                String fullPath = String.join("/", reference.getCdkPath()) + "/" + lastPath;
                allPaths.add(fullPath);
            } else {
                Optional<String> any = document.getPidPaths().stream().filter(path -> path.startsWith(reference.getReferencedPid())).findAny();
                if (any.isPresent()) {
                    String fullPath = String.join("/", reference.getCdkPath()) + "/" + any.get();
                    allPaths.add(fullPath);
                }
            }
        }
        return new SearchDocumentUpdate(
                document.getCompositeId(),
                document.getPid(),
                new ArrayList<>(allPaths),
                new ArrayList<>(inCollections),
                direct);
    }

    private List<List<String>> getCDKPathsFromRoots(String collectionPid) {
        List<List<String>> result = new ArrayList<>();
        // Cesta začínající přímo danou sbírkou.
        result.add(Collections.singletonList(collectionPid));
        // Všechny cesty přes rodiče.
        result.addAll(buildCDKPaths(collectionPid, new LinkedHashSet<>()));
        return deduplicatePaths(result);
    }

    private List<List<String>> deduplicatePaths(List<List<String>> paths) {
        Map<String, List<String>> unique = new LinkedHashMap<>();
        for (List<String> path : paths) {
            unique.putIfAbsent(String.join("/", path), path);
        }
        return new ArrayList<>(unique.values());
    }

    private List<List<String>> buildCDKPaths(String collectionPid, Set<String> visited) {
        if (!visited.add(collectionPid)) {
            throw new IllegalStateException("Cycle in CDK collection hierarchy at " + collectionPid);
        }
        List<String> parents = processingIndex.getParentCollections(collectionPid);
        if (parents == null || parents.isEmpty()) {
            List<String> rootPath = new ArrayList<>();
            rootPath.add(collectionPid);
            List<List<String>> result = new ArrayList<>();
            result.add(rootPath);
            return result;
        }
        List<List<String>> result = new ArrayList<>();
        for (String parentPid : parents) {
            // Každá větev dostane vlastní množinu navštívených sbírek.
            List<List<String>> parentPaths = buildCDKPaths(parentPid, new LinkedHashSet<>(visited));
            for (List<String> parentPath : parentPaths) {
                List<String> path = new ArrayList<>(parentPath);
                path.add(collectionPid);
                result.add(path);
            }
        }
        return result;
    }


    private String removeCdkPrefix(String pid) {
        String pidStripped = new CDKReference(pid).getPid();
        return pidStripped;
    }

}
