package ca.thoughtwire.concurrent;

import com.hazelcast.core.ISemaphore;

import java.util.concurrent.TimeUnit;

/**
 * Hazelcast implementation of a distributed semaphore. Delegates to an {@link com.hazelcast.core.ISemaphore}.
 *
 * @author vanessa.williams
 */
public class HazelcastSemaphore implements DistributedSemaphore {

    private final ISemaphore delegate;

    @Override
    public String getName()
    {
        return delegate.getName();
    }

    public HazelcastSemaphore(ISemaphore delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void acquire() throws InterruptedException {
        delegate.acquire();
    }

    @Override
    public boolean tryAcquire() {
        return delegate.tryAcquire();
    }

    @Override
    public boolean tryAcquire(long l, TimeUnit timeUnit) throws InterruptedException {
        return delegate.tryAcquire(l, timeUnit);
    }

    @Override
    public void release() {
        delegate.release();
    }

    @Override
    public int availablePermits() {
        return delegate.availablePermits();
    }
}
