package io.github.zap.game.data;

import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.Serialize;
import io.github.zap.util.ConverterNames;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.util.Vector;

import java.util.Set;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpawnpointData extends DataSerializable {
    /**
     * Whether or not this spawnpoint is part of a window
     */
    boolean insideWindow;

    /**
     * The location of this spawnpoint
     */
    Vector spawn;

    /**
     * If the spawnpoint is a window spawnpoint, this is the vector to which mobs should pathfind after being spawned
     */
    Vector target;

    /**
     * This represents all of the mobs that can be spawned here
     */
    @Serialize(converter = ConverterNames.MYTHIC_MOB_SET_CONVERTER)
    Set<MythicMob> whitelist;

    private SpawnpointData() {}
}
