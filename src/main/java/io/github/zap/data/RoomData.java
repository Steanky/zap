package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
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

    private RoomData() {}
}
