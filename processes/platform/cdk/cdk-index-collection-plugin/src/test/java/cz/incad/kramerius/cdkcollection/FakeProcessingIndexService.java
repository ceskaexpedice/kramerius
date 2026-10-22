package cz.incad.kramerius.cdkcollection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FakeProcessingIndexService implements ProcessingIndexService {

    private final Set<String> cdkCollections = new LinkedHashSet<>();
    private final Map<String, Set<String>> parents = new LinkedHashMap<>();
    private final Map<String, List<String>> references = new LinkedHashMap<>();

    public void addCollection(String parentPid, String childPid) {
        cdkCollections.add(parentPid);
        if (childPid != null) {
            cdkCollections.add(childPid);
            parents.computeIfAbsent(childPid, key -> new LinkedHashSet<>()).add(parentPid);
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
    public List<String> getParentCollections(String collectionPid) {
        Set<String> collectionParents = parents.get(collectionPid);
        return collectionParents == null ? new ArrayList<>() : new ArrayList<>(collectionParents);
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