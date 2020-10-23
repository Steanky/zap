package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@AllArgsConstructor
@Getter
public class MapData extends DataSerializable {
    private String name;
    private String displayName;
    private Material displayIcon; //TODO: add converter so we can serialize materials
    private List<DoorData> doors;
    private List<RoomData> rooms;

    private MapData() {}
}
