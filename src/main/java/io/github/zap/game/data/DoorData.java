package io.github.zap.game.data;

import io.github.zap.game.MultiBoundingBox;
import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.NoSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
public class DoorData extends DataSerializable {
    @Getter
    private MultiBoundingBox doorBounds;

    @Getter
    private Set<DoorSide> sides;

    @Getter
    @Setter
    @NoSerialize
    private boolean open;

    private DoorData() {}
}
