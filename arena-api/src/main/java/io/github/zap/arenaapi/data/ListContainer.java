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
    private final String name;

    private Collection<Integer> keys;

    @Override
    public String name() {
        return name;
    }

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
        list.set(key, value);
    }

    @Override
    public void add(V value) {
        list.add(value);
    }

    @Override
    public Collection<Integer> keys() {
        keys = new ArrayList<>(list.size());

        for(int i = 0; i < list.size(); i++) {
            keys.add(i);
        }

        return keys;
    }

    @Override
    public Collection<V> values() {
        return list;
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return list.iterator();
    }
}
