package cz.incad.kramerius.cdk.index;

import java.util.*;

public class FakeSearchIndexService implements SearchIndexService {
    private final Map<String, SearchDocument> documents = new LinkedHashMap<>();
    private final Map<String, List<String>> children = new HashMap<>();

    public void addDocument(String pid) {
        addDocument(new SearchDocument(pid, new ArrayList<String>(), null));
    }

    public void addDocument(String pid, List<String> inCollections, String inCollectionsDirect) {
        addDocument(new SearchDocument(pid, inCollections, inCollectionsDirect));
    }

    public void addDocument(SearchDocument document) {
        documents.put(document.getPid(), document);
        /*
         * Parent/child relationships are maintained separately
         * by setParent().
         */
    }

    public void setParent(String childPid, String parentPid) {
        List<String> childList = children.get(parentPid);
        if (childList == null) {
            childList = new ArrayList<>();
            children.put(parentPid, childList);
        }
        if (!childList.contains(childPid)) {
            childList.add(childPid);
        }
    }

    @Override
    public SearchDocument get(String pid) {
        return documents.get(pid);
    }

    @Override
    public List<String> getChildren(String pid) {
        List<String> result = children.get(pid);
        if (result == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(result);
    }

    @Override
    public SearchDocumentPage findDocuments(String cursor, int limit) {

        /*
         * Fake cursor implementation.
         *
         * We use the PID of the last returned document as the cursor.
         */
        List<SearchDocument> all = new ArrayList<>(documents.values());

        /*
         * Deterministic ordering, similar to what we need
         * from Solr when using cursorMark.
         */
        Collections.sort(all,
                new Comparator<SearchDocument>() {
                    @Override
                    public int compare(SearchDocument a, SearchDocument b) {
                        return a.getPid().compareTo(b.getPid());
                    }
                });

        int start = 0;
        if (cursor != null) {
            for (int i = 0; i < all.size(); i++) {
                if (cursor.equals(all.get(i).getPid())) {
                    start = i + 1;
                    break;
                }
            }
        }
        if (start >= all.size()) {
            return new SearchDocumentPage(Collections.<SearchDocument>emptyList(), null, false);
        }
        int end = Math.min(start + limit, all.size());
        List<SearchDocument> page = new ArrayList<>(all.subList(start, end));
        String nextCursor = page.get(page.size() - 1).getPid();
        boolean hasMore = end < all.size();
        return new SearchDocumentPage(page, nextCursor, hasMore);
    }

    @Override
    public void updateCollectionsFields(String pid, List<String> inCollections, String inCollectionsDirect) {
        SearchDocument document = documents.get(pid);
        if (document == null) {
            throw new IllegalArgumentException("Document not found: " + pid);
        }
        document.setInCollections(new ArrayList<>(inCollections));
        document.setInCollectionsDirect(inCollectionsDirect);
    }

}