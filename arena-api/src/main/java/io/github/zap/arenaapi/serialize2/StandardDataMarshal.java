package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.util.*;

class StandardDataMarshal implements DataMarshal {
    private record NodeContext(NodeContext previous, Map<String, Object> map, DataKey name) { }

    private static final DataContainer EMPTY = new StandardDataContainer(new HashMap<>());
    private final KeyFactory factory;

    StandardDataMarshal(@NotNull KeyFactory factory) {
        this.factory = factory;
    }

    @Override
    public @NotNull DataContainer marshalData(@NotNull Map<String, Object> mappings) {
        if(isMapNode(mappings)) {
            StandardDataContainer topLevel = new StandardDataContainer(mappings);
            Queue<Runnable> postProcess = new ArrayDeque<>();
            Stack<NodeContext> pending = new Stack<>();
            Map<Map<?, ?>, StandardDataContainer> mapToContainer = new IdentityHashMap<>(); //we don't care about equals()

            pending.push(new NodeContext(null, mappings, null));
            mapToContainer.put(mappings, topLevel);

            while(!pending.empty()) { //using a stack here supports recursive behavior without the overhead
                NodeContext current = pending.pop();

                for(Map.Entry<String, Object> entry : current.map.entrySet()) {
                    Object value = entry.getValue();

                    //only try to check inside maps
                    if(value instanceof Map<?, ?> map) {
                        if(mapToContainer.containsKey(map) || isMapNode(map)) { //we are a child node
                            //noinspection unchecked
                            Map<String, Object> stringObjectMap = (Map<String, Object>)map; //not actually unsafe

                            //properly resolve references to container instances that have already been checked
                            StandardDataContainer container = mapToContainer.get(map);
                            if(container == null) {
                                pending.push(new NodeContext(current, stringObjectMap, factory.makeRaw(entry.getKey())));
                                container = new StandardDataContainer(stringObjectMap);
                                mapToContainer.put(map, container);
                            }

                            StandardDataContainer finalContainer = container;
                            postProcess.add(() -> current.map.replace(entry.getKey(), finalContainer));
                        }
                    }
                }

                while(!postProcess.isEmpty()) { //apply modifications to map
                    postProcess.remove().run();
                }
            }

            return topLevel;
        }

        return EMPTY;
    }

    private boolean isMapNode(Map<?, ?> map) { //ensure valid keys while building data
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
