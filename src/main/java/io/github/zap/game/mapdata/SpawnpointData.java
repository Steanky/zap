package io.github.zap.game.mapdata;

import io.github.zap.serialize.DataSerializable;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.Set;

@AllArgsConstructor
public class SpawnpointData extends DataSerializable {
    @Getter
    private Vector spawn;

    @Getter
    private Vector target;

    @Getter
    private Set<MythicMob> whitelist;

    private SpawnpointData() {}

    public boolean canSpawn(MythicMob mob) {
        return whitelist.contains(mob);
    }
}
