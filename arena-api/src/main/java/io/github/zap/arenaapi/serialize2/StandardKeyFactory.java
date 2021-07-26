package io.github.zap.arenaapi.serialize2;

import org.jetbrains.annotations.NotNull;

class StandardKeyFactory implements KeyFactory {
    private static final String DELIMITER = ":";

    StandardKeyFactory() {}

    private String[] getComponents(String key) {
        if(!key.isEmpty()) {
            String[] split = key.split(DELIMITER);

            if(split.length == 2 && validNamespaceAndName(split[0], split[1])) {
                return split;
            }
        }

        return null;
    }

    private boolean validNamespaceAndName(String namespace, String name) {
        return !namespace.isEmpty() && !name.isEmpty() && !namespace.contains(DELIMITER) && !name.contains(DELIMITER);
    }

    @Override
    public boolean validKeySyntax(@NotNull String key) {
        return getComponents(key) != null;
    }

    @Override
    public @NotNull DataKey make(@NotNull String namespace, @NotNull String name) {
        if(validNamespaceAndName(namespace, name)) {
            return new StandardDataKey(namespace.concat(DELIMITER).concat(name), namespace.length());
        }

        throw new IllegalArgumentException("Invalid key syntax for namespace " + namespace + " and name " + name);
    }

    @Override
    public @NotNull DataKey makeRaw(@NotNull String raw) {
        String[] components = getComponents(raw);
        if(components != null) {
            String namespace = components[0];
            String name = components[1];

            if(validNamespaceAndName(namespace, name)) {
                return new StandardDataKey(namespace.concat(DELIMITER).concat(name), namespace.length());
            }
        }

        throw new IllegalArgumentException("Invalid key syntax: " + raw);
    }
}
