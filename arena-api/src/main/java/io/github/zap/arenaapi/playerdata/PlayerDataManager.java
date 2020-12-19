package io.github.zap.arenaapi.playerdata;

import java.util.UUID;

public interface PlayerDataManager {
    /**
     * Returns the PlayerData for the specified UUID. This function should not return null if the UUID does not
     * have a data entry; rather, it should return an object initialized to default values. This function may
     * return null if an error occurs when attempting to load the data. It may not throw exceptions.
     * @param id The ID of the player
     * @return The player's PlayerData
     */
    PlayerData getPlayerData(UUID id);

    /**
     * Saves all PlayerData that is stored in memory.
     */
    void flushAll();
}
