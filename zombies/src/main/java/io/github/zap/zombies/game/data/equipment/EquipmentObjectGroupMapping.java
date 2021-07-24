package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.equipment.EquipmentObjectGroup;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Mapping to create {@link EquipmentObjectGroup}s from a {@link OfflinePlayer} and the slots it manages
 */
@FunctionalInterface
public interface EquipmentObjectGroupMapping {

    /**
     * Creates an equipment object group
     * @param player The player to create the equipment object group for
     * @param slots The slots used by the equipment object group
     * @return The new equipment object group
     */
    @NotNull EquipmentObjectGroup createEquipmentObjectGroup(@NotNull OfflinePlayer player, @NotNull Set<Integer> slots);

}
