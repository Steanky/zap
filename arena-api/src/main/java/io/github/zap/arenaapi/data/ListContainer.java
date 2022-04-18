package io.github.zap.arenaapi.data;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
public class ListContainer<V> implements Container<Integer, V> {
    private final List<V> list;
    private final Class<V> valueClass;

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public V get(Integer value) {
        return list.get(value);
    }

    @Override
    public void set(Integer key, V value) {
        if(key == list.size()) {
            list.add(value);
        }
        else {
            list.set(key, value);
        }
    }

    @Override
    public void add(V value) {
        list.add(value);
    }

    @Override
    public boolean remove(Integer key) {
        list.remove((int)key);
        return true;
    }

    @Override
    public boolean fixedSize() {
        return false;
    }

    @Override
    public boolean readOnly() {
        return false;
    }

    @Override
    public boolean supportsIndexing() {
        return true;
    }

    @Override
    public boolean canAppend() {
        return true;
    }

    @Override
    public Collection<Integer> keys() {
        Collection<Integer> keys = new ArrayList<>(list.size());

        for(int i = 0; i < list.size(); i++) {
            keys.add(i);
        }

        return keys;
    }

    @Override
    public Collection<V> values() {
        return list;
    }

    @Override
    public Class<Integer> keyClass() {
        return Integer.class;
    }

    @Override
    public Class<V> valueClass() {
        return valueClass;
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return list.iterator();
    }
}
