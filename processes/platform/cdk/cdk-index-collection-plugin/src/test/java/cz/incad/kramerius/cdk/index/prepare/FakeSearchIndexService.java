package cz.incad.kramerius.cdk.index.prepare;

import java.util.*;

class FakeSearchIndexService
        implements SearchIndexService {

    private final Map<String, SearchDocument> documents =
            new HashMap<String, SearchDocument>();

    private final Map<String, List<String>> children =
            new HashMap<String, List<String>>();

    public void addDocument(String pid) {

        documents.put(
                pid,
                new SearchDocument(pid));
    }

    public void addChild(
            String parent,
            String child) {

        List<String> list =
                children.get(parent);

        if (list == null) {
            list = new ArrayList<String>();
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

        List<String> result =
                children.get(pid);

        return result != null
                ? result
                : Collections.<String>emptyList();
    }

    @Override
    public void updateCollectionFields(
            String pid,
            Object inCollection,
            Object inCollectionDirect) {

        SearchDocument document =
                documents.get(pid);

        if (document == null) {
            throw new IllegalArgumentException(
                    "Document not found: " + pid);
        }

        document.setField(
                "in-collection",
                inCollection);

        document.setField(
                "in-collection-direct",
                inCollectionDirect);
    }
}