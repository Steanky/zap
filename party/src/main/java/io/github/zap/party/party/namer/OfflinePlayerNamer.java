package io.github.zap.party.party.namer;

import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a {@link Component} for {@link OfflinePlayer}s.
 */
@FunctionalInterface
public interface OfflinePlayerNamer {

    /**
     * Names an {@link OfflinePlayer}.
     * @param player The {@link OfflinePlayer} to name
     * @return The name
     */
    @NotNull Component name(@NotNull OfflinePlayer player);

}
