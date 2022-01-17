package ca.thoughtwire.concurrent;

import com.hazelcast.core.IAtomicLong;

/**
 * Hazelcast implementation of a distributed atomic long. Delegates to an {@link com.hazelcast.core.IAtomicLong}.
 *
 * @author vanessa.williams
 */
public class HazelcastAtomicLong implements DistributedAtomicLong {

    private IAtomicLong delegate;

    public HazelcastAtomicLong(IAtomicLong delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public long addAndGet(long l) {
        return delegate.addAndGet(l);
    }

    @Override
    public boolean compareAndSet(long l, long l2) {
        return delegate.compareAndSet(l, l2);
    }

    @Override
    public long decrementAndGet() {
        return delegate.decrementAndGet();
    }

    @Override
    public long get() {
        return delegate.get();
    }

    @Override
    public long getAndAdd(long l) {
        return delegate.getAndAdd(l);
    }

    @Override
    public long getAndSet(long l) {
        return delegate.getAndSet(l);
    }

    @Override
    public long incrementAndGet() {
        return delegate.incrementAndGet();
    }

    @Override
    public long getAndIncrement() {
        return delegate.getAndIncrement();
    }

    @Override
    public void set(long l) {
        delegate.set(l);
    }


}
