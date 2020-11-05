package io.github.zap.game.data;

import io.github.zap.game.MultiBoundingBox;
import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class DoorSide extends DataSerializable {
    private int cost;
    private List<String> opensTo;
    private MultiBoundingBox triggerBounds;

    private DoorSide() {}
}
