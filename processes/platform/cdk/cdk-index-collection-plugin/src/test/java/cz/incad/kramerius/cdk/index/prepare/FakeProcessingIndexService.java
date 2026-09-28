package cz.incad.kramerius.cdk.index.prepare;

import java.util.*;

class FakeProcessingIndexService
        implements ProcessingIndexService {

    private final Map<String, List<String>> collections =
            new HashMap<String, List<String>>();

    private final Map<String, List<String>> references =
            new HashMap<String, List<String>>();

    public void addCollection(
            String parent,
            String child) {

        List<String> list =
                collections.get(parent);

        if (list == null) {
            list = new ArrayList<String>();
            collections.put(parent, list);
        }

        list.add(child);
    }

    public void addReference(
            String collection,
            String reference) {

        List<String> list =
                references.get(collection);

        if (list == null) {
            list = new ArrayList<String>();
            references.put(collection, list);
        }

        list.add(reference);
    }

    @Override
    public List<String> getContainedCollections(
            String collectionPid) {

        List<String> result =
                collections.get(collectionPid);

        return result != null
                ? result
                : Collections.<String>emptyList();
    }

    @Override
    public List<String> getContainedCdkReferences(
            String collectionPid) {

        List<String> result =
                references.get(collectionPid);

        return result != null
                ? result
                : Collections.<String>emptyList();
    }
}