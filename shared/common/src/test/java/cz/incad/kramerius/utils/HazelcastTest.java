package cz.incad.kramerius.utils;

import ca.thoughtwire.lock.DistributedLockService;
import ca.thoughtwire.lock.DistributedReentrantReadWriteLock;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.Test;


public class HazelcastTest {

    @Test
    public void testLocks() {
        Config config = new Config();
        GroupConfig groupConfig = config.getGroupConfig();
        groupConfig.setName("testUser");


// Create 3 Hazelcast server instances, they will automatically form the CP Subsystem:
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(config);

        DistributedLockService ls1 = DistributedLockService.newHazelcastLockService(hz1);
        DistributedLockService ls2 = DistributedLockService.newHazelcastLockService(hz2);


// Obtain a handle to the same lock from two different HZ instances:
        DistributedReentrantReadWriteLock hz1Lock = (DistributedReentrantReadWriteLock) ls1.getReentrantReadWriteLock("myLock");
        DistributedReentrantReadWriteLock hz2Lock = (DistributedReentrantReadWriteLock) ls2.getReentrantReadWriteLock("myLock");

// Acquire the lock on instance 1:
        hz1Lock.writeLock().lock();

// Fail to acquire the lock on instance 2:
        boolean lockedByHz2 = false;
        lockedByHz2 = hz2Lock.isWriteLocked();

       System.out.println("locked 1"+ lockedByHz2);

// Release the lock on instance 1:
        hz1Lock = (DistributedReentrantReadWriteLock) ls1.getReentrantReadWriteLock("myLock");
        hz1Lock.writeLock().unlock();

// Now Instance 2 can acquire the lock:

        lockedByHz2 = hz2Lock.isWriteLocked();

        System.out.println("locked 2"+ lockedByHz2);
    }

}
