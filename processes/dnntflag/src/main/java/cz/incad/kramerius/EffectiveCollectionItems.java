package cz.incad.kramerius;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Collection membership discovered while evaluating the effect of a license
 * change. Each collection maps to affected item PIDs that belong to it,
 * including membership inherited through collection ancestors.
 */
public final class EffectiveCollectionItems {
    private final Map<String, Set<String>> itemsByCollection = new LinkedHashMap<>();

    public void add(String collectionPid, String itemPid) {
        if (collectionPid == null || itemPid == null) {
            return;
        }
        itemsByCollection.computeIfAbsent(collectionPid, ignored -> new LinkedHashSet<>()).add(itemPid);
    }

    public Set<String> getCollectionPids() {
        return Collections.unmodifiableSet(itemsByCollection.keySet());
    }

    public Set<String> getItemPids(String collectionPid) {
        Set<String> itemPids = itemsByCollection.get(collectionPid);
        return itemPids == null ? Collections.emptySet() : Collections.unmodifiableSet(itemPids);
    }

    public Map<String, Set<String>> asMap() {
        Map<String, Set<String>> snapshot = new LinkedHashMap<>();
        itemsByCollection.forEach((collectionPid, itemPids) ->
                snapshot.put(collectionPid, Collections.unmodifiableSet(new LinkedHashSet<>(itemPids))));
        return Collections.unmodifiableMap(snapshot);
    }

    public boolean isEmpty() {
        return itemsByCollection.isEmpty();
    }
}
