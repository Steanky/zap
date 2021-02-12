package io.github.zap.arenaapi.data;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public class MapContainer<K, V> implements Container<K, V> {
    private final Map<K, V> map;
    private final String name;

    @Override
    public String name() {
        return name;
    }

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
    public Collection<K> keys() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return map.values().iterator();
    }
}
