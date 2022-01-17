package ca.thoughtwire.lock;

import ca.thoughtwire.concurrent.DistributedAtomicLong;
import ca.thoughtwire.concurrent.DistributedDataStructureFactory;
import net.jcip.annotations.ThreadSafe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import static ca.thoughtwire.lock.DistributedLockUtils.ElapsedTimer;

/**
 * <p>
 * Class to implement a strongly reentrant distributed Reader-Writer Lock.
 * Readers can always acquire the lock so long as there is no writer waiting.
 * Writers must wait until all active readers are finished.
 * </p>
 *
 * <b>This implementation guarantees that writers will not starve, but
 * readers may.</b>
 *
 * @author vanessa.williams
 */
@ThreadSafe
public class DistributedReentrantReadWriteLock implements ReadWriteLock {

    /**
     * Constructor for a distributed multiple-reader, single-writer
     * lock which guarantees that writers do not starve.
     *
     * Not intended to be used directly. Use {@link DistributedLockService#getReentrantReadWriteLock(String)}
     * instead, to take care of providing a concrete implementation of DistributedDataStructureFactory.
     *
     * @param lockService manager of node locks
     * @param lockName name of the lock
     * @throws NullPointerException if either argument is null
     */
    protected DistributedReentrantReadWriteLock(final DistributedLockService lockService, final String lockName) {

        if (lockService == null || lockName == null) throw new NullPointerException("All arguments required.");

        this.lockService = lockService;
        this.lockImpl = new LockImpl(lockService.getDistributedDataStructureFactory(), lockName);
        this.lockName = lockName;
        this.readerLock = new ReadLock(this);
        this.writerLock = new WriteLock(this);
    }

    @Override
    public Lock readLock() { return readerLock; }

    @Override
    public Lock writeLock() { return writerLock; }

    /**
     * This method is very, very dangerous. It should not be used by
     * developers at all. Only the LockService should use it when a
     * node *dies* and it wants to unlock any locks it holds. If a lock
     * is forcibly unlocked this way, it cannot be used again in the same
     * VM. Therefore it should only be used when the VM is going/gone away
     * for good.
     * @see {DistributedLockService#releaseLocks()}
     */
    protected void forceUnlock()
    {
        lockImpl.forceUnlock();
    }

    /**
     * @return true if the current thread holds the lock
     */
    public boolean isHeldByCurrentThread()
    {
        return (lockImpl.numberOfThreads.get() != 0);
    }

    /**
     * @return true if anyone has the write lock.
     */
    public boolean isWriteLocked()
    {
        return (lockImpl.isWriteLocked.get() == LockImpl.TRUE);
    }

    /**
     * @return the name of this lock.
     */
    public String getLockName() { return lockName; }

    /**
     * Useful only for testing, debugging
     * @return the number of local holds on the lock
     */
    protected int getHoldCount()
    {
        return lockImpl.holds.get().count;
    }

    /**
     * Useful only for testing, debugging
     * @return the number of local holds on the read lock
     */
    protected int getReadHoldCount()
    {
        return ((DistributedReentrantReadWriteLock.ReadLock)readLock()).getHoldCount();
    }

    /**
     * Useful only for testing, debugging
     * @return the number of local holds on the write lock
     */
    protected int getWriteHoldCount()
    {
        return ((DistributedReentrantReadWriteLock.WriteLock)writeLock()).getHoldCount();
    }

    /**
     * Useful only for testing, debugging
     * @return the local threads waiting for a lock
     */
    protected Collection<Thread> getQueuedThreads()
    {
        return lockImpl.queuedThreads;
    }

    /**
     * Useful only for testing, debugging
     * @return true if the given thread is waiting for a lock.
     */
    protected boolean hasQueuedThread(final Thread t)
    {
        return lockImpl.queuedThreads.contains(t);
    }

    /* convenience method used throughout */
    private Thread getThread() {
        return Thread.currentThread();
    }

    /* convenience methods used by debugging functions */
    private boolean addToQueuedThreads() {
        return lockImpl.queuedThreads.add(getThread());
    }

    private boolean removeFromQueuedThreads() {
        return lockImpl.queuedThreads.remove(getThread());
    }

    class LockImpl {

        LockImpl(final DistributedDataStructureFactory grid, final String lockName)
        {
            this.monitor = grid.getLock(getMonitorName(lockName));
            this.lockAvailable = grid.getCondition(monitor, getConditionName(lockName));
            this.writersWaiting = grid.getAtomicLong(getWritersWaitingName(lockName));
            this.isWriteLocked = grid.getAtomicLong(getIsWriteLockedName(lockName));
            this.numberOfThreads = grid.getAtomicLong(getNumberOfThreadsName(lockName));
        }

        /**
         * Acquire a write lock.
         *
         * @throws InterruptedException if the current thread is interrupted
         */
        void acquireExclusive() throws InterruptedException
        {
            if (getThread().isInterrupted()) throw new InterruptedException();

            monitor.lockInterruptibly();
            final long tid = getThread().getId();
            if (writeLockedBy == tid)
            {
                holds.get().count++;
            }
            else
            {
                writersWaiting.incrementAndGet();
                while (numberOfThreads.get() > 0)
                {
                    try {
                        // TODO: make this a timed wait with some reasonable timeout and throw a RuntimeException if signal not received
                        lockAvailable.await();
                    } catch (InterruptedException e) {
                        writersWaiting.decrementAndGet();
                        throw e;
                    }
                }
                writersWaiting.decrementAndGet();
                holds.get().count = 1;
                writeLockedBy = tid;
                isWriteLocked.set(TRUE);
                numberOfThreads.incrementAndGet();
            }
            monitor.unlock();

        }

        /**
         * Try and acquire an exclusive lock if it can be done in the time allowed.
         *
         * @param l timeout amount
         * @param timeUnit timeout units
         * @return true if lock acquired; false o/w or if timeout is <= 0
         * @throws InterruptedException
         */
        boolean tryAcquireExclusive(final long l, final TimeUnit timeUnit) throws InterruptedException
        {
            if (getThread().isInterrupted()) throw new InterruptedException();

            if (l <= 0) return false;

            final ElapsedTimer timer = new ElapsedTimer(timeUnit.toMillis(l));
            if (!monitor.tryLock(timer.remainingMillis(), TimeUnit.MILLISECONDS)) return false;

            final long tid = getThread().getId();
            if (writeLockedBy == tid) {
                holds.get().count++;
            } else {
                writersWaiting.incrementAndGet();
                while (numberOfThreads.get() > 0) {
                    try {
                        if (!lockAvailable.await(timer.remainingMillis(), TimeUnit.MILLISECONDS)) {
                            writersWaiting.decrementAndGet();
                            monitor.unlock();
                            return false;
                        }
                    } catch (InterruptedException e) {
                        writersWaiting.decrementAndGet();
                        throw e;
                    }
                }
                writersWaiting.decrementAndGet();
                holds.get().count = 1;
                writeLockedBy = tid;
                isWriteLocked.set(TRUE);
                numberOfThreads.incrementAndGet();
            }
            monitor.unlock();
            return true;
        }

        /**
         * Acquire a read lock.
         *
         * @throws InterruptedException if the current thread is interrupted
         */
        void acquireShared() throws InterruptedException
        {
            if (getThread().isInterrupted()) throw new InterruptedException();

            monitor.lockInterruptibly();
            if (holds.get().count > 0)
            {
                holds.get().count++;
            }
            else
            {
                // the checking of these variables is guarded by the monitor which all methods acquire
                while (!(writersWaiting.get() == 0 && isWriteLocked.get() == FALSE))
                {
                    // TODO: make this a timed wait with some reasonable timeout and throw a RuntimeException if signal not received
                    lockAvailable.await();
                }
                holds.get().count = 1;
                numberOfThreads.incrementAndGet();
            }
            monitor.unlock();


        }

        /**
         * Acquire a shared lock only if it can be done in the given time.
         * @param l timeout amount
         * @param timeUnit timeout units
         * @return true if the exclusive lock was acquired; false o/w or if timeout was <= 0
         * @throws InterruptedException if the thread is interrupted
         */
        boolean tryAcquireShared(final long l, final TimeUnit timeUnit) throws InterruptedException
        {
            if (getThread().isInterrupted()) throw new InterruptedException();

            if (l <= 0) return false;

            final ElapsedTimer timer = new ElapsedTimer(timeUnit.toMillis(l));

            if (!monitor.tryLock(timer.remainingMillis(), TimeUnit.MILLISECONDS)) return false;
            if (holds.get().count > 0)
            {
                holds.get().count++;
            }
            else
            {
                // the checking of these variables is guarded by the monitor which all methods acquire
                while (!(writersWaiting.get() == 0 && isWriteLocked.get() == FALSE))
                {
                    if (!lockAvailable.await(timer.remainingMillis(), TimeUnit.MILLISECONDS))
                    {
                        monitor.unlock();
                        return false;
                    }
                }
                holds.get().count = 1;
                numberOfThreads.incrementAndGet();
            }
            monitor.unlock();
            return true;
        }

        /**
         * Release a read or write lock.
         *
         * @throws IllegalMonitorStateException if the current thread holds no locks
         */
        void release()
        {
            monitor.lock();
            try {
                if (holds.get().tryDecrement() == 0)
                {
                    numberOfThreads.decrementAndGet();
                    writeLockedBy = NONE;
                    isWriteLocked.set(FALSE);
                    holds.remove();
                    lockAvailable.signalAll();
                }
            } finally {
                monitor.unlock();
            }
        }

        void forceUnlock()
        {
            monitor.lock();
            try {
                isWriteLocked.set(FALSE);
                numberOfThreads.decrementAndGet();
                lockAvailable.signalAll();
            } finally {
                monitor.unlock();
            }

        }

        private String getMonitorName(String lockName)
        {
            return PREFIX + lockName + "_reentrant";
        }

        private String getConditionName(String lockName)
        {
            return PREFIX + lockName + "_reentrant_availableCondition";
        }

        private String getWritersWaitingName(String lockName)
        {
            return PREFIX + lockName + "_reentrant_writers";
        }

        private String getIsWriteLockedName(String lockName)
        {
            return PREFIX + lockName + "_reentrant_isWriteLocked";
        }

        private String getNumberOfThreadsName(String lockName)
        {
            return PREFIX + lockName + "+reentrant_numberOfThreads";
        }
        /**
        * Per-thread lock counter to prevent unlocking by non-owners.
        */
        final ThreadLocal<HoldCounter> holds = new ThreadLocal<HoldCounter>() {
            @Override
            protected HoldCounter initialValue() { return new HoldCounter(); }
        };

        static final long NONE = 0;
        static final long TRUE = 1;
        static final long FALSE = 0;

        static final String PREFIX = "HZLOCK_";

        final Lock monitor;
        final Condition lockAvailable;
        final DistributedAtomicLong writersWaiting, isWriteLocked, numberOfThreads;

        /* local threads waiting on a lock; useful for debugging */
        final Collection<Thread> queuedThreads = Collections.synchronizedCollection(new ArrayList<Thread>());
        long writeLockedBy = NONE;

    }

    /**
     * Implementation of the reader lock.
     *
     * {@link java.util.concurrent.locks.Lock#tryLock()},
     * {@link java.util.concurrent.locks.Lock#tryLock(long, java.util.concurrent.TimeUnit)} and
     * {@link java.util.concurrent.locks.Lock#newCondition()} are not supported.
     */
    public class ReadLock implements Lock
    {
        public ReadLock(final DistributedReentrantReadWriteLock readWriteLock)
        {
            this.lockImpl = readWriteLock.lockImpl;
        }

        @Override
        public void lock() {
            addToQueuedThreads();
            try {
                lockImpl.acquireShared();
                readHolds.get().count++;
            } catch (InterruptedException e) {
                // restore interrupt rather than swallowing or rethrowing InterruptedException
                getThread().interrupt();
            } finally {
                removeFromQueuedThreads();
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException
        {
            addToQueuedThreads();
            try {
                lockImpl.acquireShared();
                readHolds.get().count++;
            } finally {
                removeFromQueuedThreads();
            }
        }

        @Override
        public void unlock() {
            lockImpl.release();
            if (--readHolds.get().count == 0)
             {
                 readHolds.remove();
             }
        }

        @Override
        public boolean tryLock() {
            throw new UnsupportedOperationException(
                    "Untimed tryLock not supported; use tryLock(long l, TimeUnit timeUnit) instead");
        }

        @Override
        public boolean tryLock(final long l, final TimeUnit timeUnit) throws InterruptedException {
            addToQueuedThreads();
            try {
                final boolean result = lockImpl.tryAcquireShared(l, timeUnit);
                if (result) readHolds.get().count++;
                return result;
            } finally {
                removeFromQueuedThreads();
            }
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException("Operation not supported.");
        }

        /**
         * @return the number of local threads holding the read lock
         */
        public int getHoldCount()
        {
            return readHolds.get().count;
        }

        private final LockImpl lockImpl;

        /**
        * Per-thread read lock counter
        */
        final ThreadLocal<HoldCounter> readHolds = new ThreadLocal<HoldCounter>() {
            @Override
            protected HoldCounter initialValue() { return new HoldCounter(); }
        };
    }

    /**
     * Implementation of the writer lock.
     *
     * {@link java.util.concurrent.locks.Lock#tryLock()},
     * {@link java.util.concurrent.locks.Lock#tryLock(long, java.util.concurrent.TimeUnit)} and
     * {@link java.util.concurrent.locks.Lock#newCondition()} are not supported.
     */
    public class WriteLock implements Lock
    {
        public WriteLock(final DistributedReentrantReadWriteLock readWriteLock)
        {
            this.lockImpl = readWriteLock.lockImpl;
        }

        @Override
        public void lock() {
            addToQueuedThreads();
            try {
                lockImpl.acquireExclusive();
                lockService.addNodeLock(lockName);
                writeHolds.get().count++;
            } catch (InterruptedException e) {
                // restore interrupt rather than swallowing or rethrowing InterruptedException
                getThread().interrupt();
            } finally {
                removeFromQueuedThreads();
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            addToQueuedThreads();
            try {
                lockImpl.acquireExclusive();
                lockService.addNodeLock(lockName);
                writeHolds.get().count++;
            } finally {
                removeFromQueuedThreads();
            }
        }

        @Override
        public void unlock()
        {
            lockImpl.release();
            lockService.removeNodeLock(lockName);
            if (--writeHolds.get().count == 0)
            {
                writeHolds.remove();
            }
        }

        @Override
        public boolean tryLock() {
            throw new UnsupportedOperationException(
                    "Untimed tryLock not supported; use tryLock(long l, TimeUnit timeUnit) instead");
        }

        @Override
        public boolean tryLock(final long l, final TimeUnit timeUnit) throws InterruptedException {
            addToQueuedThreads();
            try {
                final boolean result = lockImpl.tryAcquireExclusive(l, timeUnit);
                if (result)
                {
                    lockService.addNodeLock(lockName);
                    writeHolds.get().count++;
                }
                return result;
            } finally {
                removeFromQueuedThreads();
            }
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException("Operation not supported.");
        }

        /**
         * @return the number of local threads holding the write lock (should only ever be 0 or 1)
         */
        public int getHoldCount()
        {
            return writeHolds.get().count;
        }

        /**
         * @return whether the current thread owns the write lock
         */
        public boolean isHeldByCurrentThread()
        {
            return lockImpl.isWriteLocked.get() == getThread().getId();
        }

        private final LockImpl lockImpl;

        /**
        * Per-thread write lock counter
        */
        final ThreadLocal<HoldCounter> writeHolds = new ThreadLocal<HoldCounter>() {
            @Override
            protected HoldCounter initialValue() { return new HoldCounter(); }
        };
    }

    /*
    * Counter for per-thread lock hold counts. Maintained as a ThreadLocal.
    */
    static final class HoldCounter
    {
        int count;

        /*
         * Convenience method to detect illegal attempts to release locks that are not held
         * by the calling thread.
         */
        public int tryDecrement()
        {
            count--;
            if (count < 0)
            {
                count = 0;
                throw new IllegalMonitorStateException("Current thread is not the holder of the lock.");
            }
            return count;
        }
    }

    private final String lockName;
    private final ReadLock readerLock;
    private final WriteLock writerLock;
    final LockImpl lockImpl;
    final DistributedLockService lockService;
}
