package cz.incad.kramerius.cdkcollection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FakeProcessingIndexService implements ProcessingIndexService {

    private final Set<String> cdkCollections = new LinkedHashSet<>();
    private final Map<String, String> parents = new LinkedHashMap<>();
    private final Map<String, List<String>> references = new LinkedHashMap<>();

    public void addCollection(String parentPid, String childPid) {
        cdkCollections.add(parentPid);
        if(childPid != null) {
            cdkCollections.add(childPid);
            parents.put(childPid, parentPid);
        }
    }

    public void addReference(String collectionPid, String referencedPid) {
        cdkCollections.add(collectionPid);
        List<String> refs = references.computeIfAbsent(collectionPid, key -> new ArrayList<>());
        if (!refs.contains(referencedPid)) {
            refs.add(referencedPid);
        }
    }

    @Override
    public List<String> getCollections() {
        return new ArrayList<>(cdkCollections);
    }

    @Override
    public List<String> getReferences(String collectionPid) {
        List<String> refs = references.get(collectionPid);
        return refs == null ? new ArrayList<>() : new ArrayList<>(refs);
    }

    @Override
    public String getParentCollection(String collectionPid) {
        return parents.get(collectionPid);
    }

    @Override
    public boolean isCollection(String collectionPid) {
        return cdkCollections.contains(collectionPid);
    }

    public void removeReference(String collectionPid, String referencedPid) {
        List<String> refs = references.get(collectionPid);
        if (refs != null) {
            refs.remove(referencedPid);
        }
    }

}