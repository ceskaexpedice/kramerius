package ca.thoughtwire.concurrent;

import java.util.Collection;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Interface for creating distributed data structures.
 *
 * @author vanessa.williams
 */
public interface DistributedDataStructureFactory {

    /**
     * Get a cluster-unique and static ID for the current node
     * @return some string uniquely identifiying this node in its cluster
     */
    String getNodeId();

    /**
     * @param name the semaphore name
     * @param initPermits the number of initial permits
     * @return a named semaphore, created & initialized with requested permits if necessary.
     */
    DistributedSemaphore getSemaphore(String name, int initPermits);

    /**
     * @param name the atomic long name
     * @return a named atomic long
     */
    DistributedAtomicLong getAtomicLong(String name);

    /**
     * @param name the lock name
     * @return a named lock
     */
    Lock getLock(String name);

    /**
     * @param lock a lock
     * @param conditionName a name for the condition
     * @return a new condition
     */
    Condition getCondition(Lock lock, String conditionName);

    /**
     * @param name the map name
     * @param <K> the key type
     * @param <V> the value type
     * @return a named MultiMap
     */
    <K,V> DistributedMultiMap<K,V> getMultiMap(String name);

    /**
     * Register to receive notification of nodes joining the cluster
     * @param listener object to receive notifications
     */
    void addMembershipListener(GridMembershipListener listener);

    /**
     * Register to receive notification of nodes leaving the cluster
     * @param listener object to receive notifications
     */
    void removeMembershipListener(GridMembershipListener listener);

    /**
     * @return all registered listeners, in an unmodifiable collection
     */
    Collection<GridMembershipListener> getListeners();

}
