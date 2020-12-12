package io.github.zap.zombies.game.mob;

import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.SpawnpointData;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.exceptions.InvalidMobTypeException;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;

public class RangedSpawner implements Spawner {
    @Override
    public boolean canSpawn(ZombiesArena arena, SpawnpointData spawnpoint, MythicMob mob) {
        int minRangeSq = arena.getMap().getSpawnRadiusSquared();

        if(spawnpoint.getWhitelist().contains(mob)) {
            for(ZombiesPlayer player : arena.getZombiesPlayers()) {
                double dSq = spawnpoint.getSpawn().distanceSquared(player.getPlayer().getLocation().toVector());

                if(dSq <= minRangeSq) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void spawnAt(ZombiesArena arena, SpawnpointData spawnpoint, MythicMob mob) {
        try {
            MythicMobs.inst().getAPIHelper().spawnMythicMob(mob, WorldUtils.locationFrom(arena.getWorld(),
                    spawnpoint.getTarget()), 0);
        } catch (InvalidMobTypeException e) {
            Zombies.warning(String.format("InvalidMobException when trying to spawn mob with internal name %s",
                    mob.getInternalName()));
        }
    }
}
