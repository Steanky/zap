package io.github.zap.game.data;

import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.Serialize;
import io.github.zap.util.ConverterNames;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.Set;

@AllArgsConstructor
@Getter
public class SpawnpointData extends DataSerializable {
    private boolean insideWindow;

    private Vector spawn;

    private Vector target;

    @Serialize(converter = ConverterNames.MYTHIC_MOB_SET_CONVERTER)
    private Set<MythicMob> whitelist;

    private SpawnpointData() {}
}
