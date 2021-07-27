package io.github.zap.arenaapi.serialize2;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

class StandardKeyFactory implements KeyFactory {
    private static final StandardKeyFactory instance = new StandardKeyFactory();

    private static final String DELIMITER = ":";

    private StandardKeyFactory() {}

    public static KeyFactory instance() {
        return instance;
    }

    private String[] getComponents(String key) {
        if(!key.isEmpty()) {
            String[] split = key.split(DELIMITER);

            if(split.length == 1 || split.length == 2) {
                String namespace = split.length == 1 ? StringUtils.EMPTY : split[0];
                String name = split.length == 1 ? split[0] : split[1];

                if(validNamespaceAndName(namespace, name)) {
                    return new String[] { namespace, name };
                }
            }
        }

        return null;
    }

    private boolean validNamespaceAndName(String namespace, String name) {
        return !name.isEmpty() && !namespace.contains(DELIMITER) && !name.contains(DELIMITER);
    }

    private DataKey makeInternal(String namespace, String name) {
        return new StandardDataKey(namespace.isEmpty() ? name : namespace.concat(DELIMITER).concat(name), namespace.length());
    }

    @Override
    public boolean validKeySyntax(@NotNull String key) {
        return getComponents(key) != null;
    }

    @Override
    public @NotNull DataKey make(@NotNull String namespace, @NotNull String name) {
        if(validNamespaceAndName(namespace, name)) {
            return makeInternal(namespace, name);
        }

        throw new IllegalArgumentException("Invalid key syntax for namespace " + namespace + " and/or name " + name);
    }

    @Override
    public @NotNull DataKey makeRaw(@NotNull String raw) {
        String[] components = getComponents(raw);
        if(components != null) {
            String namespace = components[0];
            String name = components[1];

            return makeInternal(namespace, name);
        }

        throw new IllegalArgumentException("Invalid key syntax for string: " + raw);
    }
}
