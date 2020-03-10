package ca.thoughtwire.lock;

import ca.thoughtwire.concurrent.DistributedDataStructureFactory;
import ca.thoughtwire.concurrent.DistributedMultiMap;
import ca.thoughtwire.concurrent.GridMembershipListener;
import ca.thoughtwire.concurrent.HazelcastDataStructureFactory;
import com.hazelcast.core.HazelcastInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A factory for distributed locks.
 *
 * @author vanessa.williams
 */
public class DistributedLockService implements GridMembershipListener {

    /**
     * Static factory method for creating a lock service instance.
     *
     * @param distributedDataStructureFactory factory for distributed data structures
     * @return a new lock service instance
     */
    public static DistributedLockService newLockService(
            final DistributedDataStructureFactory distributedDataStructureFactory)
    {
        if (distributedDataStructureFactory == null)
        {
            throw new IllegalArgumentException("DistributedDataStructureFactory argument is required.");
        }
        final DistributedLockService distributedLockService =
                new DistributedLockService(distributedDataStructureFactory);
        distributedLockService.getDistributedDataStructureFactory().addMembershipListener(distributedLockService);
        return distributedLockService;
    }

    /**
     * Convenience static factory method for creating a lock factory using Hazelcast.
     * A shortcut for new DistributedLockService(new HazelcastDataStructureFactory(hazelcastInstance))).
     *
     * @param hazelcastInstance  the grid instance
     * @return A DistributedLockService based on a HazelcastDataStructureFactory.
     */
    public static DistributedLockService newHazelcastLockService(final HazelcastInstance hazelcastInstance)
    {
        if (hazelcastInstance == null)
        {
            throw new IllegalArgumentException("HazelcastInstance argument is required.");
        }
        final HazelcastDataStructureFactory dataStructureFactory =
                HazelcastDataStructureFactory.getInstance(hazelcastInstance);
        return newLockService(dataStructureFactory);
    }

    /*
     * Constructor.
     *
     * The constructor is protected because a static factory method is required in order to
     * prevent the "this" reference from escaping during construction (see Java Concurrency
     * in Practice, Section 3.2.1 Safe construction practices)
     *
     * @param distributedDataStructureFactory factory for creating distributed semaphores and atomic primitives
     */
    protected DistributedLockService(final DistributedDataStructureFactory distributedDataStructureFactory)
    {
        if (distributedDataStructureFactory == null)
        {
            throw new IllegalArgumentException("DistributedDataStructureFactory argument is required.");
        }
        this.distributedDataStructureFactory = distributedDataStructureFactory;
        this.nodeId = distributedDataStructureFactory.getNodeId();
        this.nodesToExclusiveLocks = distributedDataStructureFactory.getMultiMap(getLockMapName());
        this.lockServiceLock = distributedDataStructureFactory.getLock(getServiceLockName());
    }

    /**
     * @param lockName name of the lock
     * @return a re-entrant distributed readers-writers lock
     */
    public ReadWriteLock getReentrantReadWriteLock(final String lockName)
    {
        if (THREAD_LOCKS.containsKey(lockName))
        {
            return THREAD_LOCKS.get(lockName);
        }
        else {
            final DistributedReentrantReadWriteLock lock = new DistributedReentrantReadWriteLock(this, lockName);
            THREAD_LOCKS.put(lockName, lock);
            return lock;
        }

    }

    protected void addNodeLock(final String lockName)
    {
        nodesToExclusiveLocks.put(nodeId, lockName);
    }

    protected void removeNodeLock(final String lockName)
    {
        nodesToExclusiveLocks.remove(nodeId, lockName);
    }

    /**
     * @return data structure factory (grid)
     */
    public DistributedDataStructureFactory getDistributedDataStructureFactory()
    {
        return distributedDataStructureFactory;
    }

    @Override
    public void memberAdded(final String uuid) {
        // NO OP
    }

    @Override
    public void memberRemoved(final String uuid) {
        // ensure that only one node does the cleanup activity
        if (lockServiceLock.tryLock()) {
            try {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        releaseLocks(uuid);
                    }
                });
            } finally {
                lockServiceLock.unlock();
            }
        }
    }

    public void shutdown()
    {
        if (!shutdown) {
            shutdown = true;
            distributedDataStructureFactory.removeMembershipListener(this);
            executorService.shutdown();
            try {
                executorService.awaitTermination(TIMEOUT, UNIT);
            } catch (InterruptedException ignore) {
                // we're shutting down anyway
                Thread.currentThread().interrupt();
            }
        }
    }

    private void releaseLocks(final String uuid)
    {
        final Collection<String> locksHeldByNode = nodesToExclusiveLocks.get(uuid);
        for (String lockName: locksHeldByNode)
        {
            final DistributedReentrantReadWriteLock lock =
                    (DistributedReentrantReadWriteLock)getReentrantReadWriteLock(lockName);
            lock.forceUnlock();
        }
        nodesToExclusiveLocks.remove(uuid);
    }

    private static String getLockMapName() { return PREFIX + "exclusiveLocks"; }

    private static String getServiceLockName() { return PREFIX + "lockServiceSingleton"; }

    private static final String PREFIX = "HZLOCK_";

    private static final Map<String, DistributedReentrantReadWriteLock> THREAD_LOCKS =
            new HashMap<String, DistributedReentrantReadWriteLock>();

    /* Structures used to release locks held by crashed nodes */
    private final String nodeId;
    private final DistributedMultiMap<String, String> nodesToExclusiveLocks;
    private final Lock lockServiceLock;

    /* for responding to membership events */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final long TIMEOUT = 2000;
    private static final TimeUnit UNIT = TimeUnit.MILLISECONDS;

    private volatile boolean shutdown = false;

    final DistributedDataStructureFactory distributedDataStructureFactory;

}
