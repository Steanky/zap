package io.github.zap.game.data;

import io.github.zap.game.MultiBoundingBox;
import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.NoSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
public class RoomData extends DataSerializable {
    @Getter
    private String name;

    @Getter
    private String displayName;

    @Getter
    private MultiBoundingBox bounds;

    @Getter
    private Set<WindowData> windows;

    @Getter
    private Set<SpawnpointData> spawnpoints;

    @Getter
    private boolean isSpawn;

    private RoomData() {}
}