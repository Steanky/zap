package io.github.zap.arenaapi.util;

import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MetadataHelper {
    /**
     * Returns the named metadata for the specific entity, belonging to the specific plugin.
     * @param target The object for which to retrieve metadata
     * @param plugin The plugin to which the metadata belongs
     * @param metadataName The name of the metadata to retrieve
     * @return The metadata, or null if it could not be found or there was a type mismatch.
     */
    public static @Nullable MetadataValue getMetadataFor(@NotNull Metadatable target, @NotNull Plugin plugin,
                                                         @NotNull String metadataName) {
        for(MetadataValue value : target.getMetadata(metadataName)) {
            if(value.getOwningPlugin() == plugin) {
                return value;
            }
        }

        return null;
    }

    public static MetadataEntry getMetadataEntryFor(@NotNull Metadatable target, @NotNull Plugin plugin, @NotNull String metadataName) {
        return new MetadataEntry(plugin, metadataName, getMetadataFor(target, plugin, metadataName));
    }

    /**
     * Sets metadata on the given target, with the given name, for the given plugin. If the plugin already has metadata
     * with that name associated with it, the value is replaced.
     * @param target The target to set metadata for
     * @param metadataName The name of the metadata
     * @param plugin The plugin owning the metadata
     * @param value The object used as metadata
     */
    public static void setMetadataFor(@NotNull Metadatable target, @NotNull String metadataName, @NotNull Plugin plugin, @Nullable Object value) {
        target.setMetadata(metadataName, new FixedMetadataValue(plugin, value));
    }

    public static void setMetadataFor(@NotNull Metadatable target, @NotNull MetadataEntry entry) {
        target.setMetadata(entry.getMetadataName(), new FixedMetadataValue(entry.getOwningPlugin(), entry.getMetadataValue()));
    }
}
