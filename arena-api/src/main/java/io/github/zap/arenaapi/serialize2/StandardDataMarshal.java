package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;

class StandardDataMarshal implements DataMarshal {
    private record NodeContext(NodeContext previous, Map<String, Object> map, DataKey name) { }

    private record ConverterEntry(Converter<?> converter, Class<?> from, List<ConverterEntry> subclasses) {
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

    private static final DataContainer EMPTY = new StandardDataContainer(new HashMap<>(), ConverterRegistry.DEFAULT);

    private final KeyFactory factory;
    private final List<ConverterEntry> deserializers = new ArrayList<>();
    private final ConverterRegistry converterRegistry;

    StandardDataMarshal(@NotNull KeyFactory factory, @NotNull ConverterRegistry converterRegistry) {
        this.factory = factory;
        this.converterRegistry = converterRegistry;
    }

    StandardDataMarshal(@NotNull KeyFactory factory) {
        this.factory = factory;
        this.converterRegistry = this;

        initDefaultConverters();
    }

    private void initDefaultConverters() {
        converterRegistry.registerDeserializer(new ConverterBase<>(Collection.class) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            @Override
            public Object convert(@NotNull Collection collection, @NotNull Class<?> type) {
                Object newArray = Array.newInstance(type.getComponentType(), collection.size());

                int index = 0;
                for(Object assign : collection) {
                    Class<?> convertingFrom = assign.getClass();
                    Class<?> componentType = type.getComponentType();

                    if(!componentType.isAssignableFrom(convertingFrom)) { //deep convert arrays if needed
                        Converter converter = deserializerFor(convertingFrom, componentType);

                        if(converter != null && converter.canConvertTo(componentType)) {
                            assign = converter.convert(assign, convertingFrom);

                            if(assign == null) {
                                return null;
                            }
                        }
                        else {
                            return null;
                        }
                    }

                    Array.set(newArray, index++, assign);
                }

                return newArray;
            }

            @Override
            public boolean canConvertTo(@NotNull Class<?> type) {
                return type.isArray();
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
                            if(container == null) {
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
        entry.subclasses.add(new ConverterEntry(converter, from, new ArrayList<>()));
    }

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
