package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

class StandardDataMarshal implements DataMarshal {
    private record NodeContext(NodeContext previous, Map<String, Object> map, DataKey name) { }

    private record ConverterEntry(Converter<?> converter, Class<?> from, Set<ConverterEntry> subclasses) {
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ConverterEntry otherEntry) {
                return otherEntry.from.equals(from);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(from);
        }
    }

    private static final DataContainer EMPTY = new StandardDataContainer(new HashMap<>(), ConverterRegistry.EMPTY_REGISTRY);

    private final KeyFactory factory;
    private final Set<ConverterEntry> deserializers = new HashSet<>();
    private final ConverterRegistry converterRegistry;

    StandardDataMarshal(@NotNull KeyFactory factory, @NotNull ConverterRegistry converterRegistry) {
        this.factory = factory;
        this.converterRegistry = converterRegistry;
    }

    StandardDataMarshal(@NotNull KeyFactory factory) {
        this.factory = factory;
        this.converterRegistry = this;

        registerConverters();
    }

    private void registerConverters() {
        converterRegistry.registerDeserializer(new ConverterBase<>(Collection.class) {
            @SuppressWarnings({"rawtypes"})
            @Override
            public Object convert(@NotNull Collection collection, @NotNull TypeInformation typeInformation) {
                Class<?> type = typeInformation.type();

                if(type.isArray()) {
                    Class<?> componentType = type.getComponentType();
                    Object newArray = Array.newInstance(componentType, collection.size());

                    int index = 0;
                    for(Object assign : collection) {
                        assign = convertElement(converterRegistry, assign, new TypeInformation<>(componentType));

                        if(assign != null) {
                            Array.set(newArray, index++, assign);
                        }
                        else { //element conversion fail
                            return null;
                        }
                    }

                    return newArray;
                }
                else if(Collection.class.isAssignableFrom(type)) {
                    try {
                        return super.convertCollection(converterRegistry, collection, typeInformation);
                    }
                    catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                            IllegalAccessException ignored) { }
                }

                return null;
            }

            @Override
            public boolean canConvertTo(@NotNull Class<?> type) {
                return type.isArray() || Collection.class.isAssignableFrom(type);
            }
        });

        converterRegistry.registerDeserializer(new ConverterBase<>(Map.class) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            @Override
            public Object convert(@NotNull Map map, @NotNull TypeInformation typeInformation) {
                Class<?> type = typeInformation.type();

                try {
                    Constructor constructor = type.getDeclaredConstructor();
                    Map newMap = (Map)constructor.newInstance();

                    TypeInformation[] typeParameters = typeInformation.parameters();
                    TypeInformation keyType = null;
                    TypeInformation valueType = null;

                    if(typeParameters.length == 2) {
                        keyType = typeParameters[0];
                        valueType = typeParameters[1];
                    }

                    for (Map.Entry entry : (Iterable<Map.Entry>) map.entrySet()) {
                        Object newValue = convertElement(converterRegistry, entry.getValue(), valueType);
                        Object newKey = convertElement(converterRegistry, entry.getKey(), keyType);

                        if (newValue != null && newKey != null) {
                            newMap.put(newKey, newValue);
                        } else {
                            return null;
                        }
                    }

                    return newMap;
                }
                catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                        IllegalAccessException ignored) { }

                return null;
            }

            @Override
            public boolean canConvertTo(@NotNull Class<?> type) {
                return Map.class.isAssignableFrom(type);
            }
        });

        converterRegistry.registerDeserializer(new ConverterBase<>(Double.class) {
            @Override
            public Object convert(@NotNull Double value, @NotNull TypeInformation typeInformation) {
                if(value % 1 == 0) {
                    return value.intValue();
                }

                return null;
            }

            @Override
            public boolean canConvertTo(@NotNull Class<?> type) {
                return type.equals(Integer.class);
            }
        });
    }

    @Override
    public @NotNull DataContainer fromMappings(@NotNull Map<String, Object> mappings) {
        if(isMapNode(mappings)) {
            StandardDataContainer topLevel = new StandardDataContainer(mappings, converterRegistry);
            Queue<Runnable> postProcess = new ArrayDeque<>();
            Deque<NodeContext> pending = new ArrayDeque<>();
            Map<Map<?, ?>, StandardDataContainer> mapToContainer = new IdentityHashMap<>(); //we don't care about equals()

            pending.push(new NodeContext(null, mappings, null));
            mapToContainer.put(mappings, topLevel);

            while(!pending.isEmpty()) { //using a stack here supports recursive behavior without the overhead
                NodeContext current = pending.removeLast();

                for(Map.Entry<String, Object> entry : current.map.entrySet()) {
                    Object value = entry.getValue();

                    //only try to check inside maps
                    if(value instanceof Map<?, ?> map) {
                        if(mapToContainer.containsKey(map) || isMapNode(map)) { //we are a child node
                            //noinspection unchecked
                            Map<String, Object> stringObjectMap = (Map<String, Object>)map; //not actually unsafe

                            //properly resolve references to container instances that have already been checked
                            StandardDataContainer container = mapToContainer.get(map);
                            if(container == null) { //only "recurse" on new maps
                                pending.push(new NodeContext(current, stringObjectMap, factory.makeRaw(entry.getKey())));
                                container = new StandardDataContainer(stringObjectMap, converterRegistry);
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

    @Override
    public @NotNull Map<String, Object> toMappings(@NotNull DataContainer container) {
        Deque<Map<String, Object>> pending = new ArrayDeque<>();
        Queue<Runnable> operations = new ArrayDeque<>();
        Map<String, Object> topLevel = container.objectMapping();
        Map<DataContainer, Map<String, Object>> dataMap = new IdentityHashMap<>();

        pending.push(topLevel);

        while(!pending.isEmpty()) {
            Map<String, Object> current = pending.removeLast();

            for(Map.Entry<String, Object> entry : current.entrySet()) {
                Object value = entry.getValue();

                if(value instanceof DataContainer dataContainer) {
                    Map<String, Object> map = dataMap.get(dataContainer);
                    if(map == null) {
                        map = dataContainer.objectMapping();
                        dataMap.put(dataContainer, map);
                        pending.push(map);
                    }

                    Map<String, Object> finalMap = map;
                    operations.add(() -> current.replace(entry.getKey(), finalMap));
                }
            }

            while(!operations.isEmpty()) {
                operations.remove().run();
            }
        }


        return topLevel;
    }

    @Override
    public <From> Converter<? super From> deserializerFor(@NotNull Class<? super From> from, @NotNull Class<?> to) {
        ConverterEntry lowest = lowestEntry(from);
        if(lowest.converter != null && lowest.converter.canConvertTo(to)) { //null converter means no compatible accepting type
            //noinspection unchecked
            return (Converter<? super From>) lowest.converter; //this actually should never fail
        }

        return null;
    }

    @Override
    public void registerDeserializer(@NotNull Converter<?> converter) {
        Class<?> from = converter.convertsFrom();

        ConverterEntry entry = lowestEntry(from);
        if(!entry.subclasses.add(new ConverterEntry(converter, from, new HashSet<>()))) {
            throw new IllegalArgumentException("A converter converting from type " + converter.convertsFrom() + " has already been registered!");
        }
    }

    /**
     * Obtains the most specific converter for a given class. An example: if class B extends class A, there is a
     * converter registered for class B and A, lowestEntry will return the converter for B if the object is an
     * instance of B. Otherwise, if there is no converter registered for B, lowestEntry will return the converter for
     * A.
     */
    private ConverterEntry lowestEntry(Class<?> from) {
        Set<ConverterEntry> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<ConverterEntry> pending = new ArrayDeque<>();

        //use a ConverterEntry with a null Converter to represent the top level
        ConverterEntry lowest = new ConverterEntry(null, from, deserializers);
        pending.push(lowest);

        while(!pending.isEmpty()) { //find the most specific subclass and don't redundantly visit
            ConverterEntry list = pending.removeLast();

            for(ConverterEntry entry : list.subclasses) {
                if(!visited.contains(entry)) {
                    if(from.equals(entry.from)) { //exact match found
                        return entry;
                    }

                    if(entry.from.isAssignableFrom(from)) { //we're assignable to this entry, so check lower subclasses
                        lowest = entry;
                        pending.push(entry);
                    }

                    visited.add(entry); //don't revisit this entry
                }
            }
        }

        return lowest;
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
