package io.github.zap.zombies.game.mob;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.SpawnpointData;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.util.Vector;

public interface Spawner {
    boolean canSpawn(ZombiesArena arena, SpawnpointData spawnpoint, MythicMob mob);
    void spawnAt(ZombiesArena arena, SpawnpointData spawnpoint, MythicMob mob);
}
