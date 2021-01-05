package io.github.zap.zombies.game.data.map;

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
     * Will be interpreted as either a whitelist or blacklist
     */
    Set<String> filter = new HashSet<>();

    /**
     * Whether or not filter is a whitelist
     */
    boolean isWhitelist;

    public SpawnpointData() {}

    public boolean canSpawn(String mob) {
        if(isWhitelist) {
            return filter.contains(mob);
        }
        else {
            return !filter.contains(mob);
        }
    }
}
