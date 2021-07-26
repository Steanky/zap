package io.github.zap.arenaapi.serialize2;

import io.github.zap.arenaapi.ArenaApi;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class StandardDataMarshal implements DataMarshal {
    private final KeyFactory factory;
    private final KeyTransformer transformer;

    StandardDataMarshal(@NotNull KeyFactory factory, @NotNull KeyTransformer transformer) {
        this.factory = factory;
        this.transformer = transformer;
    }

    StandardDataMarshal(@NotNull KeyFactory factory) {
        this(factory, KeyTransformer.DO_NOTHING);
    }

    @Override
    public @NotNull DataContainer marshalData(@NotNull Map<String, Object> mappings) {
        StandardDataContainer topLevel = new StandardDataContainer(mappings);
        Stack<Map<String, Object>> pending = new Stack<>();
        Map<Map<?, ?>, StandardDataContainer> mapMap = new IdentityHashMap<>(); //we don't care about equals()

        pending.push(mappings);
        mapMap.put(mappings, topLevel);

        while(!pending.empty()) { //using a stack here supports recursive behavior without the overhead
            Queue<Runnable> postProcess = new ArrayDeque<>(); //don't modify while iterating
            Map<String, Object> current = pending.pop();

            for(Map.Entry<String, ?> entry : current.entrySet()) {
                Object value = entry.getValue();

                if(value instanceof Map<?, ?> map && validateKeys(map)) {
                    //noinspection unchecked
                    Map<String, Object> stringObjectMap = (Map<String, Object>)map; //not actually unsafe

                    //properly resolve references to container instances that have already been checked
                    StandardDataContainer container = mapMap.get(map);
                    if(container == null) {
                        transformKeys(stringObjectMap);
                        pending.push(stringObjectMap);
                        container = new StandardDataContainer(stringObjectMap);
                        mapMap.put(map, container);
                    }

                    StandardDataContainer finalContainer = container;
                    postProcess.add(() -> current.replace(entry.getKey(), finalContainer));
                }
            }

            for(Runnable runnable : postProcess) {
                runnable.run();
            }
        }

        return topLevel;
    }

    private boolean validateKeys(Map<?, ?> map) { //ensure valid keys while building data
        for(Object key : map.keySet()) {
            if(key instanceof String string) {
                if(!factory.validKeySyntax(string)) {
                    return false;
                }
            }
            else {
                return false;
            }
        }

        return true;
    }

    private void transformKeys(Map<String, Object> map) {
        Queue<Runnable> postTransform = new ArrayDeque<>();

        for(Map.Entry<String, Object> entry : map.entrySet()) {
            String newKey = transformer.transform(entry.getKey());

            if(!newKey.equals(entry.getKey())) {
                if(factory.validKeySyntax(newKey)) {
                    postTransform.add(() -> {
                        map.remove(entry.getKey());
                        map.put(newKey, entry.getValue());
                    });
                }
                else {
                    ArenaApi.warning("Invalid syntax for transformed key: " + entry.getKey() + " -> " + newKey);
                }
            }
        }

        for(Runnable runnable : postTransform) {
            runnable.run();
        }
    }
}
