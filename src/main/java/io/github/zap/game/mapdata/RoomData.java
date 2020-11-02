package io.github.zap.game.mapdata;

import io.github.zap.serialize.DataSerializable;
import javafx.geometry.BoundingBox;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
public class RoomData extends DataSerializable {
    @Getter
    private String name;

    @Getter
    private String displayName;

    @Getter
    private Set<BoundingBox> bounds;

    @Getter
    private Set<WindowData> windows;

    @Getter
    private Set<SpawnpointData> spawnpoints;

    private RoomData() {}
}