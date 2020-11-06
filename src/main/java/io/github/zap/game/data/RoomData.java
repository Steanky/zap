package io.github.zap.game.data;

import io.github.zap.game.MultiAccessor;
import io.github.zap.game.MultiBoundingBox;
import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.NoSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class RoomData extends DataSerializable {
    private String name;
    private String displayName;
    private MultiBoundingBox bounds;
    private List<WindowData> windows;
    private List<SpawnpointData> spawnpoints;
    private boolean isSpawn;

    @NoSerialize
    private final MultiAccessor<Boolean> openAccessor = new MultiAccessor<>(false);

    private RoomData() {}
}