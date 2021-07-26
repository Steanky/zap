package io.github.zap.arenaapi.util;

import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Utils for bukkit attributes
 */
public class AttributeHelper {

    /**
     * Gets a modifier on an AttributeInstance
     * @param instance The AttributeInstance to search on
     * @param attributeName The name of the attribute
     * @return The attribute modifier
     */
    public static Optional<AttributeModifier> getModifier(@NotNull AttributeInstance instance, @NotNull String attributeName) {
        return instance.getModifiers().stream().filter(attributeModifier -> attributeModifier.getName().equals(attributeName)).findAny();
    }

}
