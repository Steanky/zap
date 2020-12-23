package io.github.zap.zombies.game;

import io.github.zap.zombies.game.data2.SpawnpointData;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;

public interface Spawner {
    boolean canSpawn(ZombiesArena arena, SpawnpointData spawnpoint, MythicMob mob);
    ActiveMob spawnAt(ZombiesArena arena, SpawnpointData spawnpoint, MythicMob mob);
}
