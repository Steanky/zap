package io.github.zap.arenaapi.data;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

@RequiredArgsConstructor
public class SetContainer<V> implements Container<V, V> {
    private final Set<V> set;
    private final Class<V> valueClass;

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public V get(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(V key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(V value) {
        set.add(value);
    }

    @Override
    public boolean remove(V key) {
        return set.remove(key);
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
        return false;
    }

    @Override
    public boolean canAppend() {
        return true;
    }

    @Override
    public Collection<V> keys() {
        return set;
    }

    @Override
    public Collection<V> values() {
        return set;
    }

    @Override
    public Class<V> keyClass() {
        return valueClass;
    }

    @Override
    public Class<V> valueClass() {
        return valueClass;
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return set.iterator();
    }
}
