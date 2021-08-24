package io.github.zap.zombies.game2.spawner;

import io.github.zap.zombies.game.SpawnMethod;
import io.github.zap.zombies.game.data.map.SpawnEntryData;
import io.github.zap.zombies.game.data.map.SpawnpointData;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Spawner {

    default @NotNull List<ActiveMob> spawnMobs(@NotNull List<SpawnEntryData> mobs, @NotNull SpawnMethod method,
                                               double slaSquared, boolean randomize, boolean updateCount) {
        return spawnMobs(mobs, method, spawnpoint -> true, slaSquared, randomize, updateCount);
    }

    @NotNull List<ActiveMob> spawnMobs(@NotNull List<SpawnEntryData> mobs, @NotNull SpawnMethod method,
                                       @NotNull Predicate<SpawnpointData> spawnpointPredicate, double slaSquared,
                                       boolean randomize, boolean updateCount);

    @NotNull Optional<ActiveMob> spawnMobAt(@NotNull String mobType, @NotNull Vector vector, boolean updateCount);

    int getZombiesLeft();

}
