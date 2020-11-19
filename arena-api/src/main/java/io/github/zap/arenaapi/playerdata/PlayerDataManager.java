package io.github.zap.arenaapi.playerdata;

import java.util.UUID;

public interface PlayerDataManager {
    PlayerData getPlayerData(UUID id);
}
