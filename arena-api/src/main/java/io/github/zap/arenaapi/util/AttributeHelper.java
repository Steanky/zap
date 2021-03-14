package io.github.zap.arenaapi.util;

import org.bukkit.attribute.AttributeInstance;
import org.jetbrains.annotations.NotNull;

public class AttributeHelper {
    public static boolean hasModifier(@NotNull AttributeInstance instance, @NotNull String attributeName) {
        return instance.getModifiers().stream().anyMatch(attributeModifier -> attributeModifier.getName().equals(attributeName));
    }
}
