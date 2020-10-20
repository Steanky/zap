package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
import lombok.Getter;

public class RoomData extends DataSerializable {
    @Getter
    private String name;

    @Getter
    private String displayName;

    @Getter
    private MultiBoundingBox bounds;

    @Getter
    private WindowData[] windows;

    private RoomData() {}

    public RoomData(String name, String displayName, MultiBoundingBox bounds, WindowData[] windows) {
        this.name = name;
        this.displayName = displayName;
        this.bounds = bounds;
        this.windows = windows;
    }
}
