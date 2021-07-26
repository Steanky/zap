package io.github.zap.arenaapi.serialize2;

import io.github.zap.arenaapi.ArenaApi;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class StandardDataMarshal implements DataMarshal {
    private final KeyFactory factory;
    private final Map<TypeConverter.Signature, TypeConverter<?>> converterMap = new HashMap<>();

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

        while(!pending.empty()) { //using a stack here supports recursive behavior without the overhead
            Queue<Runnable> postProcess = new ArrayDeque<>(); //don't modify while iterating
            Map<String, Object> current = pending.pop();

            for(Map.Entry<String, ?> entry : current.entrySet()) {
                Object value = entry.getValue();

                //if we're in the hashmap, don't bother to validate keys (we know they're good)
                //otherwise check if they're syntactically valid for this data marshal
                if(value instanceof Map<?, ?> map) {
                    if(mapMap.containsKey(map) || validateKeys(map)) { //we are a child node
                        //noinspection unchecked
                        Map<String, Object> stringObjectMap = (Map<String, Object>)map; //not actually unsafe

                        //properly resolve references to container instances that have already been checked
                        StandardDataContainer container = mapMap.get(map);
                        if(container == null) {
                            pending.push(stringObjectMap);
                            container = new StandardDataContainer(stringObjectMap);
                            mapMap.put(map, container);
                        }

                        StandardDataContainer finalContainer = container;
                        postProcess.add(() -> current.replace(entry.getKey(), finalContainer));
                    }
                    else {
                        /*
                        we are a hashmap that itself contains syntactically invalid keys, so just treat it like a
                        normal value and process it
                         */
                        processValue(current, entry.getKey(), value, postProcess);
                    }
                }
                else {
                    processValue(current, entry.getKey(), value, postProcess);
                }
            }

            //apply modifications to map
            for(Runnable runnable : postProcess) {
                runnable.run();
            }
        }

        return topLevel;
    }

    @Override
    public void registerTypeConverter(@NotNull TypeConverter<?> converter) {
        TypeConverter.Signature signature = new TypeConverter.Signature(converter.convertsFrom(), converter.namespace());
        if(converterMap.containsKey(signature)) {
            throw new IllegalArgumentException("A ValueConverter for that type has already been registered.");
        }

        converterMap.put(signature, converter);
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void processValue(Map<String, Object> target, String key, Object value, Queue<Runnable> postProcess) {
        if(factory.validKeySyntax(key)) {
            DataKey dataKey = factory.makeRaw(key);
            TypeConverter.Signature signature = new TypeConverter.Signature(value.getClass(), dataKey.namespace());

            TypeConverter converter = converterMap.get(signature);
            if(converter != null) {
                postProcess.add(() -> target.replace(key, converter.convert(value)));
            }
        }
        else {
            ArenaApi.warning("Ignoring invalid key-value pair " + key + ", " + value);
            postProcess.add(() -> target.remove(key));
        }
    }
}
