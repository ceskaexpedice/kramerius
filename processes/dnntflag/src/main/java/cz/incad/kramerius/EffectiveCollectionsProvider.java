package cz.incad.kramerius;

import java.util.Set;

/**
 * Discovers collection PIDs that may be affected by a license change.
 * Implementations can use the processing index or the search index while the
 * license calculation itself remains independent of the discovery source.
 */
public interface EffectiveCollectionsProvider {
    /** Returns the affected collection PIDs for the target object. */
    Set<String> getAffectedCollections(String targetPid);
}
