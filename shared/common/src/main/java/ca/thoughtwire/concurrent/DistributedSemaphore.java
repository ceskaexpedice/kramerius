package ca.thoughtwire.concurrent;

import java.util.concurrent.TimeUnit;

/**
* Since {@link java.util.concurrent.Semaphore} is not an interface, distributed
* implementations need to be wrapped and their operations delegated. This is a universal
* interface for implementations of semaphores.
*
* @see HazelcastSemaphore
*
* @author vanessa.williams
**/
public interface DistributedSemaphore {

    /**
     * @return name of the semaphore
     */
    String getName();

    /**
     * @see java.util.concurrent.Semaphore#tryAcquire()
     */
    boolean tryAcquire();

    /**
     * @see java.util.concurrent.Semaphore#tryAcquire(long, java.util.concurrent.TimeUnit)
     */
    boolean tryAcquire(long l, TimeUnit timeUnit) throws InterruptedException;

    /**
     * @see java.util.concurrent.Semaphore#acquire()
     * @throws InterruptedException
     */
    void acquire() throws InterruptedException;

    /**
     * @see java.util.concurrent.Semaphore#release()
     */
    void release();

    /**
     * @see java.util.concurrent.Semaphore#availablePermits()
     */
    int availablePermits();
}
