package io.github.zap.zombies.game2.spawner;

import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.SpawnMethod;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.github.zap.zombies.game.data.map.SpawnpointData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitWorld;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MobManager;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class BasicSpawner implements Spawner {

    private final Plugin plugin;

    private final MobManager mobManager;

    private final World world;

    private final Set<ActiveMob> mobs;

    private final double spawnLevel;

    private int zombiesLeft;

    public BasicSpawner(@NotNull Plugin plugin, @NotNull MobManager mobManager, @NotNull World world,
                        @NotNull Set<ActiveMob> mobs, double spawnLevel) {
        this.plugin = plugin;
        this.mobManager = mobManager;
        this.world = world;
        this.mobs = mobs;
        this.spawnLevel = spawnLevel;
    }

    @Override
    public @NotNull List<ActiveMob> spawnMobs(@NotNull List<SpawnEntryData> mobs, @NotNull SpawnMethod method,
                                              @NotNull Predicate<SpawnpointData> spawnpointPredicate, double slaSquared,
                                              boolean randomize, boolean updateCount) {
        return null;
    }

    @Override
    public @NotNull Optional<ActiveMob> spawnMobAt(@NotNull String mobType, @NotNull Vector vector,
                                                   boolean updateCount) {
        Optional<ActiveMob> activeMobOptional = spawnMob(mobType, vector);

        if (activeMobOptional.isPresent()) {
            if (updateCount) {
                zombiesLeft++;
            }


        }

        return activeMobOptional;
    }

    @Override
    public int getZombiesLeft() {
        return zombiesLeft;
    }

    private @NotNull Optional<ActiveMob> spawnMob(@NotNull String mobName, @NotNull Vector blockPosition) {
        MythicMob mob = mobManager.getMythicMob(mobName);

        if (mob != null) {
            ActiveMob activeMob = mob.spawn(new AbstractLocation(new BukkitWorld(world), blockPosition.getX() +
                    0.5, blockPosition.getY(), blockPosition.getZ() + 0.5), spawnLevel);

            if (activeMob != null) {
                // getEntitySet().add(activeMob.getUniqueId());
                mobs.add(activeMob);
                // MetadataHelper.setMetadataFor(activeMob.getEntity().getBukkitEntity(), Zombies.ARENA_METADATA_NAME,
                        // Zombies.getInstance(), ZombiesArena.this);

                return Optional.of(activeMob);
            }
            else {
                plugin.getLogger().warning("An error occurred while trying to spawn mob of type '" + mobName
                        + "'.");
            }
        }
        else {
            plugin.getLogger().warning("Mob type '" + mobName + "' is not known.");
            Zombies.warning(String.format("Mob type '%s' is not known.", mobName));
        }

        return Optional.empty();
    }

    private record SpawnContext(@NotNull SpawnpointData spawnpoint, @NotNull WindowData window) {

    }

}
