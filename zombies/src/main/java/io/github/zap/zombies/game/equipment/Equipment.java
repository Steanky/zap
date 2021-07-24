package io.github.zap.zombies.game.equipment;

import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A piece of equipment in the hotbar
 * @param <D> The type of equipment data the equipment uses
 * @param <L> The type of level the equipment uses
 */
public class Equipment<D extends @NotNull EquipmentData<@NotNull L>, L extends @NotNull Object> extends HotbarObject {

    private final @NotNull ZombiesPlayer zombiesPlayer;

    private final @NotNull LocalizationManager localizationManager;

    private final @NotNull D equipmentData;

    public Equipment(@NotNull ZombiesPlayer player, int slot, @NotNull D equipmentData) {
        super(player.getPlayer(), slot);

        this.zombiesPlayer = player;
        this.localizationManager = Zombies.getInstance().getLocalizationManager();
        this.equipmentData = equipmentData;
        setRepresentingItemStack(equipmentData.createItemStack(player.getPlayer(), 0));
    }

    /**
     * Gets the current level of the equipment
     * @return The current level of the equipment
     */
    public @NotNull L getCurrentLevel() {
        return equipmentData.getLevels().get(0);
    }

    /**
     * Gets the {@link ZombiesPlayer} associated with this equipment
     * @return The player
     */
    public @NotNull ZombiesPlayer getZombiesPlayer() {
        return zombiesPlayer;
    }

    /**
     * Gets the {@link LocalizationManager} this equipment uses
     * @return The localization manager
     */
    public @NotNull LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    /**
     * Gets the data associated with this equipment
     * @return The data
     */
    public @NotNull D getEquipmentData() {
        return equipmentData;
    }

}
