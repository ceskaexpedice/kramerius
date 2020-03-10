package ca.thoughtwire.concurrent;

/**
 * Since {@link java.util.concurrent.atomic.AtomicLong} is not an interface, distributed
 * implementations need to be wrapped and their operations delegated. This is a universal
 * interface for implementations of atomic longs.
 *
 * @see HazelcastAtomicLong
 *
 * @author vanessa.williams
 */
public interface DistributedAtomicLong {

    /**
     * @return name of this atomic long
     */
    String getName();

    /**
     * @see java.util.concurrent.atomic.AtomicLong#incrementAndGet()
     */
    long incrementAndGet();

    /**
     * @see java.util.concurrent.atomic.AtomicLong#getAndIncrement()
     */
    long getAndIncrement();

    /**
     * @see java.util.concurrent.atomic.AtomicLong#decrementAndGet()
     */
    long decrementAndGet();

    /**
     * @see java.util.concurrent.atomic.AtomicLong#addAndGet(long)
     */
    long addAndGet(long delta);

    /**
     * @see java.util.concurrent.atomic.AtomicLong#getAndAdd(long)
     */
    long getAndAdd(long delta);

    /**
     * @see java.util.concurrent.atomic.AtomicLong#compareAndSet(long, long)
     */
    boolean compareAndSet(long expect, long update);

    /**
     * @see java.util.concurrent.atomic.AtomicLong#getAndSet(long)
     */
    long getAndSet(long newValue);

    /**
     * @see java.util.concurrent.atomic.AtomicLong#get()
     */
    long get();

    /**
     * @see java.util.concurrent.atomic.AtomicLong#set(long)
     */
    void set(long newValue);

}
