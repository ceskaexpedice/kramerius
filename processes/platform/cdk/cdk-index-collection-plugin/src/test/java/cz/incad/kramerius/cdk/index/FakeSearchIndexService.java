package cz.incad.kramerius.cdk.index;

import java.util.*;

class FakeSearchIndexService implements SearchIndexService {

    private final Map<String, SearchDocument> documents = new HashMap<>();
    private final Map<String, List<String>> children = new HashMap<>();

    public void addDocument(String pid) {
        documents.put(pid, new SearchDocument(pid));
    }

    public void addChild(String parent, String child) {
        List<String> list = children.get(parent);
        if (list == null) {
            list = new ArrayList<>();
            children.put(parent, list);
        }
        list.add(child);
    }

    @Override
    public SearchDocument get(String pid) {
        return documents.get(pid);
    }

    @Override
    public List<String> getChildren(String pid) {
        List<String> result = children.get(pid);
        return result != null ? result : Collections.emptyList();
    }

    @Override
    public void updateCollectionsFields(String pid, List<String> inCollections, String inCollectionsDirect) {
        SearchDocument document = documents.get(pid);
        if (document == null) {
            throw new IllegalArgumentException("Document not found: " + pid);
        }
        document.setInCollections(inCollections);
        document.setInCollectionsDirect(inCollectionsDirect);
    }
}