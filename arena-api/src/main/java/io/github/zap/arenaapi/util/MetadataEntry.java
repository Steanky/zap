package io.github.zap.arenaapi.util;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class MetadataEntry {
    private final Plugin owningPlugin;
    private final String metadataName;
    private final Object metadataValue;

    public MetadataEntry(@NotNull Plugin owningPlugin, @NotNull String metadataName, @Nullable Object metadataValue) {
        this.owningPlugin = owningPlugin;
        this.metadataName = metadataName;
        this.metadataValue = metadataValue;
    }
}
