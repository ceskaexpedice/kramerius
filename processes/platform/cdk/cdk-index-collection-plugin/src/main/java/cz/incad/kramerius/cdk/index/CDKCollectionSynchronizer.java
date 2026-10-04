package cz.incad.kramerius.cdk.index;

import java.util.*;

/**
 * Synchronizes CDK collection membership in the search index.
 *
 * <p>
 * The synchronizer never loads the complete search index into memory.
 * Source documents are processed page by page.
 */
public class CDKCollectionSynchronizer {

    public static final String CDK_PREFIX = "cdk/";

    private static final int DEFAULT_PAGE_SIZE = 500;

    private final SearchIndexService searchIndex;
    private final ProcessingIndexService processingIndex;
    private final int pageSize;

    public CDKCollectionSynchronizer(SearchIndexService searchIndex, ProcessingIndexService processingIndex) {
        this(searchIndex, processingIndex, DEFAULT_PAGE_SIZE);
    }

    public CDKCollectionSynchronizer(SearchIndexService searchIndex, ProcessingIndexService processingIndex, int pageSize) {
        this.searchIndex = searchIndex;
        this.processingIndex = processingIndex;
        this.pageSize = pageSize;
    }

    public void synchronize() {
        Set<String> cdkCollections = new HashSet<>(processingIndex.getCDKCollections());
        /*
         * sourcePid -> CDK collections which contain the source PID.
         */
        Map<String, Set<String>> sourceToCDK = new HashMap<>();
        /*
         * sourcePid -> CDK collection which directly references that source PID.
         */
        Map<String, String> sourceToDirectCDK = new HashMap<>();
        /*
         * Cache:
         *
         * source root PID -> complete source tree
         */
        Map<String, Set<String>> sourceTreeCache = new HashMap<>();
        /*
         * Cache:
         *
         * CDK collection -> effective source tree
         *
         * This includes references inherited from child CDK
         * collections.
         */
        Map<String, Set<String>> cdkSourceTreeCache = new HashMap<>();

        /*
         * First build the complete CDK -> source mapping.
         *
         * This is done only once.
         */
        for (String cdkCollection : cdkCollections) {
            Set<String> sourcePids = collectCDKSourceTrees(cdkCollection, new HashSet<>(), sourceTreeCache, cdkSourceTreeCache);
            for (String sourcePid : sourcePids) {
                Set<String> collections = sourceToCDK.get(sourcePid);
                if (collections == null) {
                    collections = new HashSet<>();
                    sourceToCDK.put(sourcePid, collections);
                }
                collections.add(cdkCollection);
            }
        }

        /*
         * Direct references are deliberately calculated separately.
         *
         * Only a reference directly belonging to a CDK collection
         * creates in-collection-direct.
         */
        for (String cdkCollection : cdkCollections) {
            List<String> references = processingIndex.getCDKReferences(cdkCollection);
            if (references == null) {
                continue;
            }
            for (String reference : references) {
                String sourcePid = removeCdkPrefix(reference);
                /*
                 * Keep the same "first one wins" behavior as the
                 * previous implementation.
                 */
                if (!sourceToDirectCDK.containsKey(sourcePid)) {
                    sourceToDirectCDK.put(sourcePid, cdkCollection);
                }
            }
        }

        /*
         * Now process the search index page by page.
         */
        String cursor = null;
        do {
            SearchDocumentPage page = searchIndex.findDocuments(cursor, pageSize);
            List<SearchDocument> documents = page.getDocuments();
            for (SearchDocument document : documents) {
                synchronizeDocument(document, cdkCollections, sourceToCDK, sourceToDirectCDK);
            }
            cursor = page.getNextCursor();
            if (!page.hasMore()) {
                break;
            }
        } while (true);
    }

    private void synchronizeDocument(SearchDocument document, Set<String> cdkCollections, Map<String, Set<String>> sourceToCDK,
                                     Map<String, String> sourceToDirectCDK) {
        String pid = document.getPid();
        /*
         * CDK collection documents themselves are not modified.
         *
         * Their structure is maintained by the CDK processing/indexing
         * side.
         */
        if (cdkCollections.contains(pid)) {
            return;
        }
        /*
         * Start with the existing normal collection memberships.
         */
        List<String> inCollections = new ArrayList<>(document.getInCollections());
        /*
         * Remove old CDK memberships.
         *
         * Non-CDK memberships remain untouched.
         */
        removeCDKValues(inCollections, cdkCollections);
        /*
         * Preserve the old direct collection only when it is a
         * valid non-CDK collection.
         */
        String direct = getNonCDKDirectCollection(document.getInCollectionsDirect(), cdkCollections);
        /*
         * Add currently valid CDK memberships.
         */
        Set<String> validCDKCollections = sourceToCDK.get(pid);
        if (validCDKCollections != null) {
            inCollections.addAll(validCDKCollections);
        }
        /*
         * Set the direct CDK collection, if this source PID is
         * directly referenced by one.
         */
        String cdkDirect = sourceToDirectCDK.get(pid);
        if (cdkDirect != null) {
            direct = cdkDirect;
        }
        searchIndex.updateCollectionsFields(pid, inCollections, direct);
    }

    /**
     * Calculates all source PIDs represented by a CDK collection,
     * including references inherited from child CDK collections.
     */
    private Set<String> collectCDKSourceTrees(
            String cdkCollection,
            Set<String> visitedCDKCollections,
            Map<String, Set<String>> sourceTreeCache,
            Map<String, Set<String>> cdkSourceTreeCache) {

        /*
         * Already calculated.
         */
        Set<String> cached = cdkSourceTreeCache.get(cdkCollection);
        if (cached != null) {
            return cached;
        }

        /*
         * Protect against cycles in the CDK hierarchy.
         */
        if (!visitedCDKCollections.add(cdkCollection)) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<>();
        /*
         * References directly belonging to this CDK collection.
         */
        List<String> references = processingIndex.getCDKReferences(cdkCollection);
        if (references != null) {
            for (String reference : references) {
                String sourcePid = removeCdkPrefix(reference);
                Set<String> sourceTree = sourceTreeCache.get(sourcePid);
                if (sourceTree == null) {
                    sourceTree = new HashSet<>();
                    collectSourceTree(sourcePid, sourceTree);
                    sourceTreeCache.put(sourcePid, sourceTree);
                }
                result.addAll(sourceTree);
            }
        }
        /*
         * References inherited from child CDK collections.
         */
        List<String> children = processingIndex.getCollections(cdkCollection);
        if (children != null) {
            for (String child : children) {
                if (!processingIndex.isCDKCollection(child)) {
                    continue;
                }
                Set<String> childSourceTree = collectCDKSourceTrees(child, visitedCDKCollections, sourceTreeCache, cdkSourceTreeCache);
                result.addAll(childSourceTree);
            }
        }
        Set<String> cachedResult = Collections.unmodifiableSet(new HashSet<>(result));
        cdkSourceTreeCache.put(cdkCollection, cachedResult);
        return cachedResult;
    }

    /**
     * Collects the complete source tree below rootPid.
     */
    private void collectSourceTree(String rootPid, Set<String> result) {
        if (!result.add(rootPid)) {
            return;
        }
        List<String> children = searchIndex.getChildren(rootPid);
        if (children == null) {
            return;
        }
        for (String child : children) {
            collectSourceTree(child, result);
        }
    }

    /**
     * Removes CDK collection PIDs from the existing
     * in-collection field.
     */
    private void removeCDKValues(List<String> inCollections, Set<String> cdkCollections) {
        Iterator<String> iterator = inCollections.iterator();
        while (iterator.hasNext()) {
            String collection = iterator.next();
            /*
             * Current CDK collection.
             */
            if (cdkCollections.contains(collection)) {
                iterator.remove();
                continue;
            }
            /*
             * Collection document no longer exists.
             *
             * This also cleans up a dangling reference to a deleted
             * CDK collection.
             */
            if (searchIndex.get(collection) == null) {
                iterator.remove();
            }
        }
    }

    /**
     * Returns the old direct value if it is a valid non-CDK
     * collection.
     */
    private String getNonCDKDirectCollection(String direct, Set<String> cdkCollections) {
        if (direct == null) {
            return null;
        }
        /*
         * Current CDK direct value.
         */
        if (cdkCollections.contains(direct)) {
            return null;
        }
        /*
         * Dangling collection reference.
         */
        if (searchIndex.get(direct) == null) {
            return null;
        }
        return direct;
    }

    private String removeCdkPrefix(String reference) {
        if (reference.startsWith(CDK_PREFIX)) {
            return reference.substring(
                    CDK_PREFIX.length());
        }
        return reference;
    }
}