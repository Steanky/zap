package io.github.zap.zombies.game.data.map;

import io.github.zap.zombies.Zombies;
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
     * A vector corresponding to the location of the window to which this spawnpoint belongs. This will be ignored if
     * the spawnpoint is not inside of a window.
     */
    Vector windowFace = new Vector();

    /**
     * Used to retrieve an object that defines the behavior of the spawnpoint (what mobs it can spawn, what mobs it
     * can't spawn, etc)
     */
    String ruleName;

    public SpawnpointData() {}

    public boolean canSpawn(String mob, MapData map) {
        SpawnRule rule = map.getSpawnRules().get(ruleName);


        if(rule != null) {
            if(rule.isBlacklist()) {
                return !rule.getMobSet().contains(mob);
            }
            else {
                return rule.getMobSet().contains(mob);
            }
        }
        else {
            Zombies.warning(String.format("SpawnRule %s does not exist. Allowing mob to spawn.", ruleName));
            return true;
        }
    }
}
