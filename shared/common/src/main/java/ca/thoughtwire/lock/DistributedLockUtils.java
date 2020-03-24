package ca.thoughtwire.lock;

import ca.thoughtwire.concurrent.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * A set of classes and methods useful for testing and mocking.
 * Putting them here instead of in the test tree allows easier
 * usage by others.
 *
 * @author vanessa.williams
 */
public class DistributedLockUtils {

    DistributedLockUtils() {}

    /**
     * Subclass of {@link DistributedReentrantReadWriteLock} to expose some
     * internals for testing.
     */
    public class PublicDistributedReentrantReadWriteLock extends DistributedReentrantReadWriteLock
    {
        public PublicDistributedReentrantReadWriteLock(DistributedLockService lockService, String lockName) {
            super(lockService, lockName);
        }

        /**
         * Protected method exposed for testing
         * @return the number of holds this thread has on the lock (read + write)
         */
        public int getHoldCount()
        {
            return super.getHoldCount();
        }

        /**
         * Protected method exposed for testing
         * @return the number of holds this thread has on the read lock
         */
        public int getReadHoldCount()
        {
            return super.getReadHoldCount();
        }

        /**
         * Protected method exposed for testing
         * @return the number of holds this thread has on the write lock
         */
        public int getWriteHoldCount()
        {
            return super.getWriteHoldCount();
        }

        /**
         * Protected method exposed for testing
         * @return the threads in this process waiting for a lock
         */
        public Collection<Thread> getQueuedThreads()
        {
            return super.getQueuedThreads();
        }

        /**
         * Protected method exposed for testing
         * @return true if the given thread is waiting for a lock
         */
        public boolean hasQueuedThread(Thread t)
        {
            return super.hasQueuedThread(t);
        }
    }

    public class PublicDistributedLockService extends DistributedLockService
    {
        public PublicDistributedLockService(HazelcastDataStructureFactory dataStructureFactory)
        {
            super(dataStructureFactory);
        }

        @Override
        public DistributedReentrantReadWriteLock getReentrantReadWriteLock(String lockName) {
            if (THREAD_LOCKS.containsKey(lockName)) {
                return THREAD_LOCKS.get(lockName);
            } else {
                PublicDistributedReentrantReadWriteLock lock =
                        new PublicDistributedReentrantReadWriteLock(this, lockName);
                THREAD_LOCKS.put(lockName, lock);
                return lock;
            }
        }

        final Map<String, PublicDistributedReentrantReadWriteLock> THREAD_LOCKS =
                new HashMap<String, PublicDistributedReentrantReadWriteLock>();
    }

    public static class LocalAtomicLong implements DistributedAtomicLong
    {
        public LocalAtomicLong(String name)
        {
            this.delegate = new AtomicLong();
            this.name = name;
        }

        @Override
        public String getName() { return name; }

        @Override
        public boolean compareAndSet(long l, long l2) { return delegate.compareAndSet(l, l2); }

        @Override
        public long get() { return delegate.get(); }

        @Override
        public void set(long l) { delegate.set(l); }

        @Override
        public long getAndSet(long l) { return delegate.getAndSet(l); }

        @Override
        public long getAndIncrement() { return delegate.getAndIncrement(); }

        public long getAndDecrement() { return delegate.getAndDecrement(); }

        @Override
        public long getAndAdd(long l) { return delegate.getAndAdd(l); }

        @Override
        public long decrementAndGet() { return delegate.decrementAndGet(); }

        @Override
        public long incrementAndGet() { return delegate.incrementAndGet(); }

        @Override
        public long addAndGet(long l) { return delegate.addAndGet(l); }

        private final AtomicLong delegate;
        private final String name;
    }

    /**
     * Extends Semaphore to provide public access to protected methods.
     */
    public static class PublicSemaphore extends Semaphore
    {
        public PublicSemaphore(int permits, boolean fair)
        {
            super(permits, fair);
        }

        public Collection<Thread> getQueuedThreads()
        {
            return super.getQueuedThreads();
        }
    }

    public static class LocalSemaphore implements DistributedSemaphore
    {
        public LocalSemaphore(String name, int permits)
        {
            this.delegate = new PublicSemaphore(permits, true);
            this.name = name;
        }

        @Override
        public String getName() { return name; }

        @Override
        public void acquire() throws InterruptedException {
            delegate.acquire();
        }

        @Override
        public void release() {
            delegate.release();
        }

        @Override
        public int availablePermits() {
            return delegate.availablePermits();
        }

        @Override
        public boolean tryAcquire() {
            return delegate.tryAcquire();
        }

        @Override
        public boolean tryAcquire(long l, TimeUnit timeUnit) throws InterruptedException {
            return delegate.tryAcquire(l, timeUnit);
        }

        private final String name;
        private final PublicSemaphore delegate;
    }

    public static class LocalLock extends ReentrantLock
    {
        public LocalLock(String lockName)
        {
            super();
            this.name = lockName;
        }

        @Override
        public Collection<Thread> getQueuedThreads()
        {
            return super.getQueuedThreads();
        }

        public String getName()
        {
            return name;
        }

        private final String name;

    }

    public static class LocalMultiMap<K, V> implements DistributedMultiMap<K, V>
    {
        public LocalMultiMap(String name)
        {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Collection<V> get(K key) {
            return backingMap.get(key);
        }

        @Override
        public boolean put(K key, V value) {
            backingMap.putIfAbsent(key, new ArrayList<V>());
            synchronized (backingMap) {
                Collection<V> values = backingMap.get(key);
                final boolean exists = values.contains(value);
                if (!exists) {
                    values.add(value);
                }
                return !exists;
            }
        }

        @Override
        public Collection<V> remove(Object key) {
            return backingMap.remove(key);
        }

        @Override
        public boolean remove(Object key, Object value) {
            synchronized (backingMap) {
                Collection<V> values = backingMap.get(key);
                return values.remove(value);
            }
        }

        @Override
        public void clear() {
            backingMap.clear();
        }

        @Override
        public boolean containsKey(K key) {
            return backingMap.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            for (Collection<V> values: backingMap.values())
            {
                if (values.contains(value))
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Set<K> keySet() {
            return backingMap.keySet();
        }

        @Override
        public int size() {
            int size = 0;
            for (Collection<V> values: backingMap.values())
            {
                size += values.size();
            }
            return size;
        }

        @Override
        public Collection<V> values() {
            Collection<V> results = new ArrayList<V>();
            for (Collection<V> values: backingMap.values())
            {
                results.addAll(values);
            }
            return results;
        }

        @Override
        public int valueCount(K key) {
            return get(key).size();
        }

        private final String name;

        private final ConcurrentMap<K, Collection<V>> backingMap = new ConcurrentHashMap<K, Collection<V>>();
    }

    public static class LocalDistributedDataStructureFactory implements DistributedDataStructureFactory
    {
        public LocalDistributedDataStructureFactory() {}

        @Override
        public DistributedSemaphore getSemaphore(String name, int initPermits) {
            semaphoreMap.putIfAbsent(name, new LocalSemaphore(name, initPermits));
            return semaphoreMap.get(name);
        }

        @Override
        public DistributedAtomicLong getAtomicLong(String name) {
            atomicLongMap.putIfAbsent(name, new LocalAtomicLong(name));
            return atomicLongMap.get(name);
        }

        @Override
        public Lock getLock(String name) {
            lockMap.putIfAbsent(name, new LocalLock(name));
            return lockMap.get(name);
        }

        /**
         * These multimaps unfortunately use raw types. Use with care.
         */
        @Override
        public <K,V> DistributedMultiMap<K,V> getMultiMap(String name) {
            multiMapMap.putIfAbsent(name, new LocalMultiMap<K,V>(name));
            //noinspection unchecked
            return multiMapMap.get(name);
        }

        @Override
        public Condition getCondition(Lock lock, String conditionName) {
            final String key = ((LocalLock) lock).getName() + ":" + conditionName;
            conditionMap.putIfAbsent(key, lock.newCondition());
            return conditionMap.get(key);
        }

        @Override
        public String getNodeId() {
            return nodeId;
        }

        @Override
        public void addMembershipListener(GridMembershipListener listener) {
            listeners.add(listener);
        }

        @Override
        public void removeMembershipListener(GridMembershipListener listener) {
            listeners.remove(listener);
        }

        @Override
        public Collection<GridMembershipListener> getListeners() {
            return Collections.unmodifiableCollection(listeners);
        }

        private final String nodeId = UUID.randomUUID().toString();
        protected final List<GridMembershipListener> listeners =
                Collections.synchronizedList(new ArrayList<GridMembershipListener>());
        private final ConcurrentMap<String, LocalAtomicLong> atomicLongMap = new ConcurrentHashMap<String, LocalAtomicLong>();
        private final ConcurrentMap<String, LocalSemaphore> semaphoreMap = new ConcurrentHashMap<String, LocalSemaphore>();
        private final ConcurrentMap<String, LocalLock> lockMap = new ConcurrentHashMap<String, LocalLock>();
        private final ConcurrentMap<String, Condition> conditionMap = new ConcurrentHashMap<String, Condition>();
        private final ConcurrentMap<String, LocalMultiMap> multiMapMap = new ConcurrentHashMap<String, LocalMultiMap>();
    }

    /**
     * Returns the number of milliseconds since time given by
     * startNanoTime, which must have been previously returned from a
     * call to {@link System#nanoTime()}.
     */
    static long millisElapsedSince(long startNanoTime) {
        return NANOSECONDS.toMillis(System.nanoTime() - startNanoTime);
    }

    /**
     * Spin-waits until lock.hasQueuedThread(t) becomes true.
     */
    void waitForQueuedThread(PublicDistributedReentrantReadWriteLock lock, Thread t) {
        long startTime = System.nanoTime();
        while (!lock.hasQueuedThread(t)) {
            if (millisElapsedSince(startTime) > LONG_DELAY_MS)
                throw new IllegalStateException("timed out");
            Thread.yield();
        }
        if(!t.isAlive()){throw new IllegalStateException("Thread "+t+" expected to be alive");}
    }

    /**
     * Spin-waits until lock.hasQueuedThread(t) becomes true.
     */
    public void waitForQueuedThread(DistributedReentrantReadWriteLock lock, Thread t)
    {
        long startTime = System.nanoTime();
        while (!lock.hasQueuedThread(t))
        {
            if (millisElapsedSince(startTime) > LONG_DELAY_MS)
            {
                throw new IllegalStateException("timed out");
            }
            Thread.yield();
        }
        if(!t.isAlive()){throw new IllegalStateException("Thread "+t+" expected to be alive");}
    }

    /**
     * Returns a new started daemon Thread running the given runnable.
     */
    Thread newStartedThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        t.start();
        return t;
    }

    /**
     * Waits for LONG_DELAY_MS milliseconds for the thread to
     * terminate (using {@link Thread#join(long)}), else interrupts
     * the thread (in the hope that it may terminate later) and fails.
     */
    void awaitTermination(Thread t) {
        awaitTermination(t, LONG_DELAY_MS);
    }

    /**
     * Waits for the specified time (in milliseconds) for the thread
     * to terminate (using {@link Thread#join(long)}), else interrupts
     * the thread (in the hope that it may terminate later) and fails.
     */
    void awaitTermination(Thread t, long timeoutMillis) {
        try {
            t.join(timeoutMillis);
        } catch (InterruptedException ie) {
            // threadUnexpectedException(ie);
            ie.printStackTrace();
        } finally {
            if (t.isAlive()) {
                t.interrupt();
                throw new IllegalStateException("Test timed out");
            }
        }
    }

    public abstract class CheckedRunnable implements Runnable {
        protected abstract void realRun() throws Throwable;

        public final void run() {
            try {
                realRun();
            } catch (Throwable t) {
                // threadUnexpectedException(t);
                t.printStackTrace();
            }
        }
    }

    public abstract class CheckedInterruptedRunnable implements Runnable {
        protected abstract void realRun() throws Throwable;

        public final void run() {
            try {
                realRun();
                throw new IllegalStateException("Thread should have thrown InterruptedException");
            } catch (InterruptedException success) {
            } catch (Throwable t) {
                // threadUnexpectedException(t);
                t.printStackTrace();
            }
        }
    }

    public static class ElapsedTimer
    {
        public ElapsedTimer(long durationMillis)
        {
            this.completedMillis = System.currentTimeMillis() + durationMillis;
        }

        public long remainingMillis()
        {
            long remainingMillis = completedMillis - System.currentTimeMillis();
            return remainingMillis >= 0 ? remainingMillis : 0;
        }

        private final long completedMillis;
    }

    static final long LONG_DELAY_MS = 2000;
    /**
     * The number of elements to place in collections, arrays, etc.
     */
    public static final int SIZE = 20;

}
