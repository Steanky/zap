package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.Serialize;
import lombok.Getter;

import java.util.Set;

public class RoomData extends DataSerializable {
    @Getter
    private String name;

    @Getter
    private String displayName;

    @Getter
    private MultiBoundingBox bounds;

    @Getter
    private Set<WindowData> windows;

    private RoomData() {}

    public RoomData(String name, String displayName, MultiBoundingBox bounds, Set<WindowData> windows) {
        this.name = name;
        this.displayName = displayName;
        this.bounds = bounds;
        this.windows = windows;
    }
}
