package io.github.zap.game.data;

import io.github.zap.event.map.DoorOpenEvent;
import io.github.zap.game.MultiBoundingBox;
import io.github.zap.game.arena.Purchasable;
import io.github.zap.game.arena.ZombiesPlayer;
import io.github.zap.serialize.DataSerializable;
import io.github.zap.util.WorldUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@AllArgsConstructor
@Getter
public class DoorSide extends DataSerializable implements Purchasable {
    private int cost;
    private List<String> opensTo;
    private MultiBoundingBox triggerBounds;

    private DoorSide() {}
}
