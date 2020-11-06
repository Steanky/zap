package io.github.zap.game.data;

import io.github.zap.game.AccessorManager;
import io.github.zap.game.MultiAccessor;
import io.github.zap.game.arena.Arena;
import io.github.zap.game.arena.ZombiesPlayer;
import io.github.zap.serialize.DataSerializable;
import io.github.zap.util.VectorUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

@AllArgsConstructor
public class MapData extends DataSerializable {
    @Getter
    private String name;

    @Getter
    private String displayName;

    @Getter
    private BoundingBox mapBounds;

    @Getter
    private int minimumCapacity;

    @Getter
    private int maximumCapacity;

    private Map<String, RoomData> rooms;

    @Getter
    private List<DoorData> doors;

    @Getter
    private List<ShopData> shops;

    @Getter
    private boolean joinableStarted;

    @Getter
    private boolean spectatorsAllowed;

    @Getter
    private boolean handRequiredToOpenDoors;

    @Getter
    private int windowRepairRadius;

    @Getter
    private int windowRepairDelay;

    @Getter
    private int windowBreakDelay;

    private MapData() { }

    /**
     * Adds a room to this MapData instance.
     * @param room The room to add
     */
    public void addRoom(RoomData room) {
        rooms.put(room.getName(), room);
    }

    /**
     * Gets the room with the specified name.
     * @param name The name of the room to retrieve
     * @return The RoomData, or null
     */
    public RoomData getRoom(String name) {
        return rooms.get(name);
    }

    /**
     * Gets all the rooms contained in this map.
     * @return The collection of rooms contained in this map
     */
    public Collection<RoomData> getRooms() {
        return rooms.values();
    }

    /**
     * Gets the window whose face contains the provided vector, or null if the vector is not inside any windows.
     * @param target The vector that may be inside of a window
     * @return The window the vector is in, or null if it's not inside anything
     */
    public WindowData windowAt(Vector target) {
        if(mapBounds.contains(target)) {
            for(RoomData roomData : rooms.values()) {
                for(WindowData window : roomData.getWindows()) {
                    if(window.getFaceBounds().contains(target)) {
                        return window;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets the window that may be within range of the specified vector. Uses Manhattan distance for fast calculations.
     * @param standing The vector used as the origin for the distance check
     * @param distance The distance limit
     * @return The WindowData, or null if there is none in range
     */
    public WindowData windowInRange(Vector standing, double distance) {
        if(mapBounds.contains(standing)) {
            for(RoomData roomData : rooms.values()) {
                for(WindowData window : roomData.getWindows()) {
                    if(VectorUtils.manhattanDistance(window.getCenter(), standing) < distance) {
                        return window;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets the door containing the specified vector.
     * @param target The vector to search for
     * @return The door whose bounds contains the specified vector, or null
     */
    public DoorData doorAt(Vector target) {
        if(mapBounds.contains(target)) {
            for(DoorData door : doors) {
                if(door.getDoorBounds().contains(target)) {
                    return door;
                }
            }
        }

        return null;
    }

    /**
     * Gets the room whose bounds contains the specified vector.
     * @param target The vector to search rooms for
     * @return The room whose bounds contains the specified vector, or null
     */
    public RoomData roomAt(Vector target) {
        if(mapBounds.contains(target)) {
            for(RoomData room : rooms.values()) {
                if(room.getBounds().contains(target)) {
                    return room;
                }
            }
        }

        return null;
    }

    public void cleanup(Arena arena) {
        AccessorManager.getInstance().removeMappingsFor(arena);
    }
}