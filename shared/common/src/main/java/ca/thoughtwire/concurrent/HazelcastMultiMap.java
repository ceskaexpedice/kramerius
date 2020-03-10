package ca.thoughtwire.concurrent;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

import java.util.Collection;
import java.util.Set;

public class HazelcastMultiMap<K,V> implements DistributedMultiMap<K,V> {

    MultiMap<K,V> delegate;

    public HazelcastMultiMap(HazelcastInstance hazelcastInstance, String name)
    {
        this.delegate = hazelcastInstance.getMultiMap(name);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    public boolean put(K key, V value) {
        return delegate.put(key, value);
    }

    public Collection<V> get(K key) {
        return delegate.get(key);
    }

    @Override
    public Collection<V> remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return delegate.remove(key, value);
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    public boolean containsKey(K key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public int valueCount(K key) {
        return delegate.valueCount(key);
    }
}
