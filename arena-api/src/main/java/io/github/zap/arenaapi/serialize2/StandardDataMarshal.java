package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.*;

class StandardDataMarshal implements DataMarshal {
    private final KeyFactory factory;

    StandardDataMarshal(@NotNull KeyFactory factory) {
        this.factory = factory;
    }

    @Override
    public @NotNull DataContainer marshalData(@NotNull Map<String, Object> mappings) {
        StandardDataContainer topLevel = new StandardDataContainer(mappings);
        Stack<Map<String, Object>> pending = new Stack<>();
        Map<Map<?, ?>, StandardDataContainer> mapMap = new IdentityHashMap<>(); //we don't care about equals()

        pending.push(mappings);
        mapMap.put(mappings, topLevel);

        while(!pending.empty()) {
            Queue<Runnable> replace = new ArrayDeque<>();
            Map<String, Object> current = pending.pop();

            for(Map.Entry<String, ?> entry : current.entrySet()) {
                Object value = entry.getValue();

                if(value instanceof Map<?, ?> map && validateKeys(map)) {
                    //noinspection unchecked
                    Map<String, Object> stringObjectMap = (Map<String, Object>)map; //not actually unsafe

                    //properly resolve cyclic references to the same container instance
                    //i don't know if this can ever happen but if so we can handle it :shrug:
                    StandardDataContainer container = mapMap.get(map);
                    if(container == null) {
                        pending.push(stringObjectMap);
                        container = new StandardDataContainer(stringObjectMap);
                        mapMap.put(map, container);
                    }

                    StandardDataContainer finalContainer = container;
                    replace.add(() -> current.replace(entry.getKey(), finalContainer));
                }
            }

            while(!replace.isEmpty()) { //don't mutate the map while iterating
                replace.poll().run();
            }
        }

        return topLevel;
    }

    private boolean validateKeys(Map<?, ?> map) { //used to identify valid keys while building data
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
}
