package org.kramerius;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Consumer;

import org.apache.commons.lang.NotImplementedException;
enum IndexType{
    Object,
    Subtree
}

public class ClassicRootMap implements Collection<TitlePidTuple> {
    HashMap<TitlePidTuple,IndexType> map = new HashMap<>();
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
    @Override
    public void forEach(Consumer<? super TitlePidTuple> action) {
        map.forEach((key, value) -> action.accept(key));
    }
    @Override
    public boolean add(TitlePidTuple tuple) {
        return map.putIfAbsent(tuple, IndexType.Object) == null;
    }
    @Override
    public boolean remove(Object o) {
        if (o instanceof TitlePidTuple tuple) {
            return map.remove(tuple) != null;
        }
        return false;
    }
    @Override
    public boolean contains(Object o) {
        if (o instanceof TitlePidTuple tuple) {
            return map.containsKey(tuple);
        }
        return false;
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
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (o instanceof TitlePidTuple t && !contains(t)) {
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean addAll(Collection<? extends TitlePidTuple> c) {
       /* boolean modified = false;
        for (TitlePidTuple tuple : c) {
            if (add(tuple)) {
                modified = true;
            }
        }
        return modified;*/throw new NotImplementedException("Not implemented in ClassicRootMap");
    }
    @Override
    public boolean removeAll(Collection<?> c) {
        /*boolean modified = false;
        for (Object o : c) {
            if (o instanceof TitlePidTuple t && remove(t)) {
                modified = true;
            }
        }
        return modified;*/
        throw new NotImplementedException("removeAll not implemented in ClassicRootMap");
    }
    @Override
    public boolean retainAll(Collection<?> c) {
        /*boolean modified = false;
        for (TitlePidTuple tuple : map.keySet()) {
            if (!c.contains(tuple)) {
                remove(tuple);
                modified = true;
            }
        }
        return modified;*/throw new NotImplementedException("Not implemented in ClassicRootMap");
    }
    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }
    @Override
    public <T> T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    }
    @Override
    public Iterator<TitlePidTuple> iterator() {
        return map.keySet().iterator();
    }

}
