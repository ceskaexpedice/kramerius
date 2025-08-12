package org.kramerius;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
enum IndexType{
    Object,
    Subtree
}
class TitleTypeTuple {
    String title;
    IndexType type;
    public TitleTypeTuple(String title, IndexType type) {
        this.title = title;
        this.type = type;
    }
}
class Tuple<X, Y> { 
  public final X x; 
  public final Y y; 
  public Tuple(X x, Y y) { 
    this.x = x; 
    this.y = y; 
  } 
} 


public class ClassicRootMap implements Map<TitlePidTuple,IndexType> {
    public HashMap<String,HashSet<String>> potentialParents = new HashMap<>();

    HashMap<String,TitleTypeTuple> map = new HashMap<>();
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
    public void forEach(Consumer <? super String> action) {
        map.keySet().forEach(action);
    }
    public boolean add(TitlePidTuple tuple) {
        return map.putIfAbsent(tuple.pid,new TitleTypeTuple(tuple.title, IndexType.Subtree)) == null;
    }
    @Override
    public IndexType put(TitlePidTuple key, IndexType value) {
        return map.put(key.pid,new TitleTypeTuple(key.title,value)).type;
    }
    public IndexType put(String pid, String title, IndexType value) {
        return map.put(pid,new TitleTypeTuple(title,value)).type;
    }
    @Override
    public IndexType putIfAbsent(TitlePidTuple key, IndexType value) {
        return map.putIfAbsent(key.pid,new TitleTypeTuple(key.title,value)).type;
    }
    @Override
    public IndexType remove(Object o) {
        throw new UnsupportedOperationException("Incorrect type");
    }
    public IndexType remove(TitlePidTuple tuple) {
        return map.remove(tuple.pid).type;
    }
    public IndexType remove(String pid) {
        var t = map.remove(pid);
        return (t == null)?null: t.type;
    }
    @Override
    public boolean containsKey(Object o) {
        throw new UnsupportedOperationException("Incorrect type");
    }
    public boolean containsKey(String pid) {
        return map.containsKey(pid);

    }
    public boolean containsKey(TitlePidTuple tuple) {
        return map.containsKey(tuple.pid);
    }
    @Override
    public int size() {
        return map.size();
    }
    @Override
    public void clear() {
        map.clear();
    }
    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("containsValue is not supported in ClassicRootMap");
    }
    @Override
    public IndexType get(Object key) {
        throw new UnsupportedOperationException("Incorrect type");
    }
    public IndexType get(TitlePidTuple tuple){
        return map.get(tuple.pid).type;
    }
    public IndexType get(String pid){
        return map.get(pid).type;
    }
    @Override
    public void putAll(Map<? extends TitlePidTuple, ? extends IndexType> m) {
        m.forEach((k, v) -> {
            if (k instanceof TitlePidTuple tuple) {
                map.put(tuple.pid, new TitleTypeTuple(tuple.title, v));
            }
        });
    }
    @Override
    public Set<TitlePidTuple> keySet() {
        return map.keySet().stream()
                .map(pid -> new TitlePidTuple(map.get(pid).title, pid))
                .collect(java.util.stream.Collectors.toSet());
    }
    @Override
    public Collection<IndexType> values() {
        return map.values().stream()
                .map(tt -> tt.type)
                .collect(java.util.stream.Collectors.toList());
    }
    @Override
    public Set<Entry<TitlePidTuple, IndexType>> entrySet() {
        return map.entrySet().stream()
                .map(entry -> new Entry<TitlePidTuple, IndexType>() {
                    @Override
                    public TitlePidTuple getKey() {
                        return new TitlePidTuple(entry.getValue().title, entry.getKey());
                    }

                    @Override
                    public IndexType getValue() {
                        return entry.getValue().type;
                    }

                    @Override
                    public IndexType setValue(IndexType value) {
                        return entry.setValue(new TitleTypeTuple(entry.getValue().title, value)).type;
                    }
                })
                .collect(java.util.stream.Collectors.toSet());
    }
}
