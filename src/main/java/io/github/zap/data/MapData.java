package io.github.zap.data;

import io.github.zap.serialize.DataSerializable;
import lombok.Getter;
import org.bukkit.Material;

public class MapData extends DataSerializable {
    @Getter
    private String name;

    @Getter
    private String displayName;

    @Getter
    private Material displayIcon; //TODO: add converter so we can serialize materials

    @Getter
    private DoorData[] doors;

    @Getter
    private RoomData[] rooms;

    private MapData() {}

    public MapData(String name, String displayName, Material displayIcon, DoorData[] doors, RoomData[] rooms) {
        this.name = name;
        this.displayName = displayName;
        this.displayIcon = displayIcon;
        this.doors = doors;
        this.rooms = rooms;
    }
}
