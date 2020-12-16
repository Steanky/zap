package io.github.zap.zombies.game;

import io.github.zap.arenaapi.util.WorldUtils;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.SpawnpointData;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.BukkitAPIHelper;
import io.lumine.xikage.mythicmobs.api.exceptions.InvalidMobTypeException;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;

public class RangedSpawner implements Spawner {
    @Override
    public boolean canSpawn(ZombiesArena arena, SpawnpointData spawnpoint, MythicMob mob) {
        if(spawnpoint.getWhitelist().contains(mob)) {
            int minRangeSq = arena.getMap().getSpawnRadiusSquared();

            for(ZombiesPlayer player : arena.getManagedPlayerMap().values()) {
                if(player.isInGame() && player.isAlive()) {
                    //mimics Hypixel's range check on zombie spawns
                    if(spawnpoint.getSpawn().distanceSquared(player.getPlayer().getLocation().toVector()) <= minRangeSq) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public ActiveMob spawnAt(ZombiesArena arena, SpawnpointData spawnpoint, MythicMob mob) {
        try {
            BukkitAPIHelper api = MythicMobs.inst().getAPIHelper();

            return api.getMythicMobInstance(api.spawnMythicMob(mob, WorldUtils.locationFrom(arena.getWorld(),
                    spawnpoint.getTarget()), arena.getMap().getMobSpawnLevel()));
        } catch (InvalidMobTypeException e) {
            Zombies.warning(String.format("InvalidMobException when trying to spawn mob with internal name %s",
                    mob.getInternalName()));
        }

        return null;
    }
}
