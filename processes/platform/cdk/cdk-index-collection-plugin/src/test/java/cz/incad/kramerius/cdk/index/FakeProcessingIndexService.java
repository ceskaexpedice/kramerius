package cz.incad.kramerius.cdk.index;

import java.util.*;

public class FakeProcessingIndexService implements ProcessingIndexService {
    private final Set<String> cdkCollections = new LinkedHashSet<>();
    private final Map<String, List<String>> collections = new HashMap<>();
    private final Map<String, List<String>> references = new HashMap<>();
    private final Map<String, String> parents = new HashMap<>();

    public void addCDKCollection(String pid) {
        cdkCollections.add(pid);
        if (!collections.containsKey(pid)) {
            collections.put(pid, new ArrayList<>());
        }
        if (!references.containsKey(pid)) {
            references.put(pid, new ArrayList<>());
        }
    }

    public void addCollection(String parentPid, String childPid) {
        addCDKCollection(parentPid);
        addCDKCollection(childPid);
        List<String> children = collections.get(parentPid);
        if (!children.contains(childPid)) {
            children.add(childPid);
        }
        parents.put(childPid, parentPid);
    }

    public void addCDKReference(String collectionPid, String reference) {
        addCDKCollection(collectionPid);
        List<String> collectionReferences = references.get(collectionPid);
        if (!collectionReferences.contains(reference)) {
            collectionReferences.add(reference);
        }
    }

    @Override
    public List<String> getCollections(String collectionPid) {
        List<String> result = collections.get(collectionPid);
        if (result == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(result);
    }

    @Override
    public List<String> getCDKReferences(String collectionPid) {
        List<String> result = references.get(collectionPid);
        if (result == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(result);
    }

    @Override
    public String getParentCollection(String collectionPid) {
        return parents.get(collectionPid);
    }

    @Override
    public boolean isCDKCollection(String collectionPid) {
        return cdkCollections.contains(collectionPid);
    }

    @Override
    public List<String> getCDKCollections() {
        return new ArrayList<>(cdkCollections);
    }

    /*
    public void removeCDKReference(
            String collectionPid,
            String referencedPid) {

        List<String> refs = references.get(collectionPid);

        if (refs != null) {
            refs.remove(referencedPid);
        }
    }

     */

    public void removeCDKReference(String source, String target) {
        List<String> list = references.get(source);
        if(list == null){
            return;
        }
        list.remove(target);
    }


}