package io.github.zap.zombies.game.data;

import io.github.zap.arenaapi.serialize.DataSerializable;
import io.github.zap.arenaapi.serialize.TypeAlias;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.util.Vector;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@TypeAlias("ZombiesBossSpawnpoint")
public class BossSpawnpoint extends DataSerializable {
    /**
     * The location of this custom spawn
     */
    Vector spawn;

    /**
     * The mob that should be spawned at the location
     */
    MythicMob mob;

    private BossSpawnpoint() {}
}
