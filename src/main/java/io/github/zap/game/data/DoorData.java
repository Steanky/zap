package io.github.zap.game.data;

import io.github.zap.game.MultiBoundingBox;
import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class DoorData extends DataSerializable {
    private MultiBoundingBox doorBounds;
    private List<DoorSide> sides;

    private DoorData() {

    }
}
