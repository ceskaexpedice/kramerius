package cz.incad.kramerius.cdk.index;

import java.util.*;

class FakeCDKProcessingIndexService implements CDKProcessingIndexService {

    private final Map<String, List<String>> collections = new HashMap<>();
    private final Map<String, List<String>> cdkReferences = new HashMap<>();

    public void addCollection(String source, String target) {
        List<String> list = collections.get(source);
        if (list == null) {
            list = new ArrayList<>();
            collections.put(source, list);
        }
        list.add(target);
    }

    public void addCDKReference(String source, String target) {
        List<String> list = cdkReferences.get(source);
        if (list == null) {
            list = new ArrayList<>();
            cdkReferences.put(source, list);
        }
        list.add(target);
    }

    @Override
    public List<String> getCollections(String collectionPid) {
        List<String> result = collections.get(collectionPid);
        return result != null ? result : Collections.emptyList();
    }

    @Override
    public List<String> getCDKReferences(String collectionPid) {
        List<String> result = cdkReferences.get(collectionPid);
        return result != null ? result : Collections.emptyList();
    }

    @Override
    public String getParentCollection(String collectionPid) {
        for (Map.Entry<String, List<String>> entry  : collections.entrySet()) {
            if (entry.getValue().contains(collectionPid)) {
                return entry.getKey();
            }
        }
        return null;
    }
}