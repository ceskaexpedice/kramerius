package ca.thoughtwire.concurrent;

import java.util.Collection;
import java.util.Set;

/**
 * Interface for a map that can have multiple values for the same key.
 *
 * @author vanessa.williams
 */
public interface DistributedMultiMap<K,V> {

    /**
     * @see com.hazelcast.core.MultiMap#getName()
     */
    public String getName();

    /**
     * @see com.hazelcast.core.MultiMap#get(Object)
     */
    public Collection<V> get(K key);

    /**
     * @see com.hazelcast.core.MultiMap#put(Object, Object)
     */
    public boolean put(K key, V value);

    /**
     * @see com.hazelcast.core.MultiMap#remove(Object)
     */
    public Collection<V> remove(Object key);

    /**
     * @see com.hazelcast.core.MultiMap#remove(Object, Object)
     */
    public boolean remove(Object key, Object value);

    /**
     * @see com.hazelcast.core.MultiMap#clear()
     */
    public void clear();

    /**
     * @see com.hazelcast.core.MultiMap#containsKey(Object)
     */
    public boolean containsKey(K key);

    /**
     * @see com.hazelcast.core.MultiMap#containsValue(Object)
     */
    public boolean containsValue(Object value);

    /**
     * @see com.hazelcast.core.MultiMap#keySet()
     */
    public Set<K> keySet();

    /**
     * @see com.hazelcast.core.MultiMap#size()
     */
    public int size();

    /**
     * @see com.hazelcast.core.MultiMap#values()
     */
    public Collection<V> values();

    /**
     * Same as get(key).size(), but may be more efficient.
     * @see com.hazelcast.core.MultiMap#valueCount(K key)
     *
     * @param key key
     * @return size of values() collection
     */
    public int valueCount(K key);

}
