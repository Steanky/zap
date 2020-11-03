package io.github.zap.game.data;

import io.github.zap.game.MultiBoundingBox;
import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
public class DoorSide extends DataSerializable {
    @Getter
    private int cost;

    @Getter
    private Set<String> openings;

    @Getter
    private MultiBoundingBox triggerBounds;

    private DoorSide() {}
}
