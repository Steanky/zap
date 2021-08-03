package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public abstract class ConverterBase<From> implements Converter<From> {
    private final Class<From> from;

    ConverterBase(Class<From> from) {
        this.from = from;
    }

    @Override
    public @NotNull Class<From> convertsFrom() {
        return from;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected @Nullable Object convertElement(@NotNull ConverterRegistry converters, @NotNull Object element,
                                           @Nullable TypeInformation elementType) {
        if(elementType == null) {
            return element;
        }

        Class<?> convertingFrom = element.getClass();
        Class<?> convertingTo = elementType.type();

        if(!convertingTo.isAssignableFrom(convertingFrom)) {
            Converter converter = converters.deserializerFor(convertingFrom, convertingTo);

            if(converter != null && converter.canConvertTo(convertingTo)) {
                element = converter.convert(element, elementType);
            }
            else {
                return null;
            }
        }

        return element;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected @Nullable Object convertCollection(@NotNull ConverterRegistry converters, Collection<?> collection,
                                              @NotNull TypeInformation convertTo) throws NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        if(Collection.class.isAssignableFrom(convertTo.type())) {
            Constructor<?> constructor = convertTo.type().getDeclaredConstructor();
            Collection newCollection = (Collection)constructor.newInstance();

            TypeInformation[] typeParameters = convertTo.parameters();
            TypeInformation elementType = typeParameters.length == 1 ? typeParameters[0] : null;

            for(Object assign : collection) {
                assign = convertElement(converters, assign, elementType);

                if(assign != null) {
                    newCollection.add(assign);
                }
                else {
                    return null;
                }
            }

            return newCollection;
        }

        return null;
    }
}
