package io.github.zap.arenaapi.data;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public class MapContainer<K, V> implements Container<K, V> {
    private final Map<K, V> map;
    private final Class<K> keyClass;
    private final Class<V> valueClass;

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public V get(K value) {
        return map.get(value);
    }

    @Override
    public void set(K key, V value) {
        map.put(key, value);
    }

    @Override
    public void add(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(K key) {
        if(map.containsKey(key)) {
            map.remove(key);
            return true;
        }

        return false;
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
        return false;
    }

    @Override
    public Collection<K> keys() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Class<K> keyClass() {
        return keyClass;
    }

    @Override
    public Class<V> valueClass() {
        return valueClass;
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return map.values().iterator();
    }
}
