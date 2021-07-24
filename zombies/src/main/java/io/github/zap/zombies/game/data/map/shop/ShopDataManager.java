package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.data.map.shop.tmtask.TeamMachineTask;
import org.jetbrains.annotations.NotNull;

/**
 * Stores and manages data about shops
 */
public interface ShopDataManager {

    // TODO: same as equipment data manager
    /**
     * Adds a shop mapping
     * @param shopType The string representation of the shop type
     * @param dataClass The class of the data the shop uses
     * @param <D> The type of the shop's data
     */
    <D extends ShopData> void addShop(@NotNull String shopType, @NotNull Class<D> dataClass);

    /**
     * Adds a team machine task
     * @param type The string representation of the team machine task type
     * @param clazz The class of the data the team machine task uses for data and execution
     */
    void addTeamMachineTask(@NotNull String type, @NotNull Class<? extends TeamMachineTask> clazz);

}
