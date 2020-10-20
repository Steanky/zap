package io.github.zap.data;

import io.github.zap.serialize.ConverterNames;
import io.github.zap.serialize.DataSerializable;
import io.github.zap.serialize.Serialize;
import lombok.Getter;
import org.bukkit.Material;

import java.util.Set;

public class MapData extends DataSerializable {
    @Getter
    private String name;

    @Getter
    private String displayName;

    @Getter
    @Serialize(converter = ConverterNames.MATERIAL_CONVERTER)
    private Material displayIcon;

    @Getter
    private Set<DoorData> doors;

    @Getter
    private Set<RoomData> rooms;

    private MapData() {}

    public MapData(String name, String displayName, Material displayIcon, Set<DoorData> doors, Set<RoomData> rooms) {
        this.name = name;
        this.displayName = displayName;
        this.displayIcon = displayIcon;
        this.doors = doors;
        this.rooms = rooms;
    }
}
