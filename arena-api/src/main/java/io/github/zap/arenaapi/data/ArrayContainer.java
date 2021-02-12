package io.github.zap.arenaapi.data;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

@RequiredArgsConstructor
public class ArrayContainer<V> implements Container<Integer, V> {
    private final V[] array;
    private final String name;

    private Collection<V> backingCollection;
    private Collection<Integer> keys;

    @Override
    public String name() {
        return name;
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public V get(Integer value) {
        return array[value];
    }

    @Override
    public void set(Integer key, V value) {
        array[key] = value;
    }

    @Override
    public Collection<Integer> keys() {
        if(keys == null) {
            keys = new ArrayList<>(array.length);

            for(int i = 0; i < array.length; i++) {
                keys.add(i);
            }
        }

        return keys;
    }

    @Override
    public Collection<V> values() {
        if(backingCollection == null) {
            backingCollection = Arrays.asList(array);
        }

        return backingCollection;
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return Arrays.stream(array).iterator();
    }
}
