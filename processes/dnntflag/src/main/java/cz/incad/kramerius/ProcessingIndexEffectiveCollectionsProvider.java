package cz.incad.kramerius;

import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.processingindex.OwnedAndFosteredChildren;
import org.ceskaexpedice.akubra.processingindex.OwnedAndFosteredParents;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexItem;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Discovers affected collections using the Akubra processing index. */
public final class ProcessingIndexEffectiveCollectionsProvider implements EffectiveCollectionsProvider {
    private static final String COLLECTION_MODEL = "collection";

    private final AkubraRepository repository;

    public ProcessingIndexEffectiveCollectionsProvider(AkubraRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        this.repository = repository;
    }

    @Override
    public Set<String> getAffectedCollections(String targetPid) {
        Set<String> result = new HashSet<>();
        if (targetPid == null || targetPid.isEmpty()) {
            return Collections.emptySet();
        }

        Deque<String> pending = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        Map<String, Set<String>> collectionAncestorsByPid = new HashMap<>();
        Map<String, Boolean> collectionModelByPid = new HashMap<>();
        pending.addLast(targetPid);
        visited.add(targetPid);

        while (!pending.isEmpty()) {
            String itemPid = pending.removeFirst();
            addCollectionParents(itemPid, result, collectionModelByPid, collectionAncestorsByPid);

            OwnedAndFosteredChildren children = repository.pi().getOwnedAndFosteredChildren(itemPid);
            List<ProcessingIndexItem> ownChildren = children == null ? null : children.own();
            if (ownChildren == null) {
                continue;
            }
            for (ProcessingIndexItem child : ownChildren) {
                if (child != null && child.targetPid() != null && visited.add(child.targetPid())) {
                    pending.addLast(child.targetPid());
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private void addCollectionParents(String itemPid,
                                      Set<String> result,
                                      Map<String, Boolean> collectionModelByPid,
                                      Map<String, Set<String>> collectionAncestorsByPid) {
        OwnedAndFosteredParents parents = repository.pi().getOwnedAndFosteredParents(itemPid);
        if (parents == null) {
            return;
        }

        ProcessingIndexItem ownParent = parents.own();
        if (ownParent != null) {
            addIfCollection(ownParent.source(), itemPid, result, collectionModelByPid, collectionAncestorsByPid);
        }

        List<ProcessingIndexItem> fosterParents = parents.foster();
        if (fosterParents != null) {
            for (ProcessingIndexItem fosterParent : fosterParents) {
                if (fosterParent != null) {
                    addIfCollection(fosterParent.source(), itemPid, result, collectionModelByPid, collectionAncestorsByPid);
                }
            }
        }
    }

    private void addIfCollection(String parentPid,
                                 String itemPid,
                                 Set<String> result,
                                 Map<String, Boolean> collectionModelByPid,
                                 Map<String, Set<String>> collectionAncestorsByPid) {
        if (parentPid != null && isCollection(parentPid, collectionModelByPid)) {
            result.add(parentPid);
            for (String ancestorPid : getCollectionAncestors(parentPid, collectionModelByPid, collectionAncestorsByPid)) {
                result.add(ancestorPid);
            }
        }
    }

    /**
     * If an affected collection is itself contained by another collection,
     * record the affected item as belonging to those collection ancestors.
     */
    private Set<String> getCollectionAncestors(String collectionPid,
                                               Map<String, Boolean> collectionModelByPid,
                                               Map<String, Set<String>> collectionAncestorsByPid) {
        Set<String> cachedAncestors = collectionAncestorsByPid.get(collectionPid);
        if (cachedAncestors != null) {
            return cachedAncestors;
        }

        Set<String> ancestors = new HashSet<>();
        Deque<String> pending = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        pending.addLast(collectionPid);
        visited.add(collectionPid);

        while (!pending.isEmpty()) {
            String itemPid = pending.removeFirst();
            OwnedAndFosteredParents parents = repository.pi().getOwnedAndFosteredParents(itemPid);
            if (parents == null) {
                continue;
            }
            ProcessingIndexItem ownParent = parents.own();
            if (ownParent != null) {
                addCollectionAncestor(ownParent.source(), ancestors, collectionModelByPid, visited, pending);
            }
            List<ProcessingIndexItem> fosterParents = parents.foster();
            if (fosterParents != null) {
                for (ProcessingIndexItem fosterParent : fosterParents) {
                    if (fosterParent != null) {
                        addCollectionAncestor(fosterParent.source(), ancestors, collectionModelByPid, visited, pending);
                    }
                }
            }
        }
        collectionAncestorsByPid.put(collectionPid, ancestors);
        return ancestors;
    }

    private void addCollectionAncestor(String parentPid,
                                       Set<String> ancestors,
                                       Map<String, Boolean> collectionModelByPid,
                                       Set<String> visited,
                                       Deque<String> pending) {
        if (parentPid == null) {
            return;
        }
        if (isCollection(parentPid, collectionModelByPid)) {
            ancestors.add(parentPid);
        }
        if (visited.add(parentPid)) {
            pending.addLast(parentPid);
        }
    }

    private boolean isCollection(String pid, Map<String, Boolean> collectionModelByPid) {
        Boolean cached = collectionModelByPid.get(pid);
        if (cached == null) {
            cached = COLLECTION_MODEL.equals(repository.pi().getModel(pid));
            collectionModelByPid.put(pid, cached);
        }
        return cached;
    }
}
