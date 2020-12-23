package io.github.zap.zombies.game.data2;

import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpawnpointData {
    /**
     * The location of this spawnpoint
     */
    Vector spawn = new Vector();

    /**
     * this is the vector to which mobs should pathfind after being spawned. if == to spawn, no pathfinding will occur
     */
    Vector target = new Vector();

    /**
     * This represents all of the mobs that can be spawned here
     */
    Set<MythicMob> whitelist = new HashSet<>();

    public SpawnpointData() {}
}
