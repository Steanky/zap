package io.github.zap.zombies.game.arena.spawner;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.game.arena.event.EntityArgs;
import io.github.zap.zombies.game.RoundContext;
import io.github.zap.zombies.game.SpawnMethod;
import io.github.zap.zombies.game.data.map.RoundData;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.github.zap.zombies.game.data.map.SpawnpointData;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Spawns {@link org.bukkit.entity.Mob}s in a Zombies game
 */
public interface Spawner {

    /**
     * Spawns an entire round of Zombies
     * @param round The round to spawn
     * @return Context for the round including spawn tasks, despawn tasks, and its spawned mobs
     */
    @NotNull RoundContext spawnRound(@NotNull RoundData round);

    /**
     * Spawns the provided SpawnEntries in this arena.
     * @param mobs The mobs to spawn
     * @param method The SpawnMethod to use
     * @param slaSquared The SLA to use
     * @param randomize Whether or not spawnpoints are randomized
     * @param updateCount Whether or not to update the total mob count
     * @return The ActiveMobs that were spawned
     */
    default @NotNull List<@NotNull ActiveMob> spawnMobs(@NotNull List<@NotNull SpawnEntryData> mobs,
                                                        @NotNull SpawnMethod method, double slaSquared,
                                                        boolean randomize, boolean updateCount) {
        return spawnMobs(mobs, method, spawnpoint -> true, slaSquared, randomize, updateCount);
    }

    /**
     * Spawns the provided SpawnEntries in this arena. Only spawnpoints that match the provided predicate can spawn
     * mobs.
     * @param mobs The mobs to spawn
     * @param method The method to use while spawning
     * @param filter The predicate used to additionally filter spawnpoints
     * @param slaSquared The value to use for SLA
     * @param randomize Whether or not spawnpoints are shuffled
     * @param updateCount Whether or not to update the total mob count
     * @return The ActiveMobs that were spawned as a result of this operation
     */
    @NotNull List<@NotNull ActiveMob> spawnMobs(@NotNull List<@NotNull SpawnEntryData> mobs,
                                                @NotNull SpawnMethod method, @NotNull Predicate<SpawnpointData> filter,
                                                double slaSquared, boolean randomize, boolean updateCount);

    /**
     * Spawns a mob at the specified vector, without performing range checks. If the mob is spawned inside of a
     * window, it will have the appropriate metadata set.
     * @param mobType The mob type to spawn
     * @param vector The vector to spawn the mob at
     * @return The mob that was spawned, or null if it failed to spawn
     */
    ActiveMob spawnMobAt(@NotNull String mobType, @NotNull Vector vector, boolean updateCount);

    /**
     * Gets a copy of the set of mobs this spawner currently manages
     * @return The set of mobs
     */
    @NotNull Set<@NotNull Mob> getMobs();

    /**
     * Gets the number of mobs left in the current {@link io.github.zap.zombies.game.data.map.RoundData round}
     * @return The number of mobs left
     */
    int getZombiesLeft();

    /**
     * Gets an event for when the number of zombies changes
     * @return The event
     */
    @NotNull Event<@NotNull ZombieCountChangedArgs> getZombieCountChangedEvent();

    /**
     * Called when a mob dies
     * @param args The args of the event
     */
    void onMobDeath(@NotNull EntityArgs<@NotNull Mob, @NotNull EntityDeathEvent> args);

    /**
     * Called when a mob is removed from the spawner's {@link org.bukkit.World}
     * @param args The args of the event
     */
    void onMobRemoveFromWorld(@NotNull EntityArgs<@NotNull Mob, @NotNull EntityRemoveFromWorldEvent> args);

}
