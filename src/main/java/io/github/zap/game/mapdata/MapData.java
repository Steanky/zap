package io.github.zap.game.mapdata;

import io.github.zap.game.Difficulty;
import io.github.zap.serialize.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class MapData extends DataSerializable {
    @Getter
    private String name;

    @Getter
    private String displayName;

    @Getter
    private Difficulty difficulty;

    @Getter
    private Set<DoorData> doors;

    private Map<String, RoomData> rooms;

    private MapData() { }

    public void addRoom(RoomData room) {
        rooms.put(room.getName(), room);
    }

    public RoomData getRoom(String name) {
        return rooms.get(name);
    }

    public Collection<RoomData> getRooms() {
        return rooms.values();
    }
}
