package io.github.zap.game.data;

import io.github.zap.serialize.DataSerializable;
import io.github.zap.util.VectorUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * This class represents a Zombies map. It is effectively a pure data class; it only contains helper functions for
 * retrieving and manipulating its own data values.
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MapData extends DataSerializable {
    /**
     * The unique name of this map that need not be user friendly
     */
    String name;

    /**
     * The user-friendly name of this map
     */
    String displayName;

    /**
     * The bounds of the map, inside which every component should exist
     */
    BoundingBox mapBounds;

    /**
     * The spawn vector of this map.
     */
    Vector spawn;

    /**
     * The minimum required number of players that this map can start with
     */
    int minimumCapacity;

    /**
     * The maximum number of players this map can hold
     */
    int maximumCapacity;

    /**
     * The duration of the game start countdown timer, in seconds
     */
    int countdownSeconds;

    /**
     * The list of rooms managed by this map
     */
    List<RoomData> rooms;

    /**
     * All the doors managed by this map
     */
    List<DoorData> doors;

    /**
     * All the shops managed by this map
     */
    List<ShopData> shops;

    /**
     * The number of coins each player should start with
     */
    int startingCoins;

    /**
     * The number of coins you get for repairing a window
     */
    int coinsOnRepair;

    /**
     * Whether or not this map should be joinable by other players after it has started
     */
    boolean joinableStarted;

    /**
     * Whether or not spectators are allowed here
     */
    boolean spectatorAllowed;

    /**
     * If this is true, players will be required to be holding nothing in order to open doors
     */
    boolean handRequiredToOpenDoors;

    /**
     * Whether or not the players should be allowed to forcibly start the game regardless of the minimum player limit
     */
    boolean forceStart;

    /**
     * The minimum (Manhattan) distance that players must be from a window in order to repair it
     */
    int windowRepairRadius;

    /**
     * The base delay, in Minecraft server ticks (20ths of a second) that occurs between window blocks being repaired
     */
    int windowRepairTicks;

    /**
     * The base rate at which mobs break through windows, in server ticks
     */
    int windowBreakTicks;

    /**
     * The material that should replace door blocks when they are opened.
     */
    Material doorFillMaterial;

    private MapData() { }

    /**
     * Gets the window whose face contains the provided vector, or null if the vector is not inside any windows.
     * @param target The vector that may be inside of a window
     * @return The window the vector is in, or null if it's not inside anything
     */
    public WindowData windowAt(Vector target) {
        if(mapBounds.contains(target)) {
            for(RoomData roomData : rooms) {
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
    public WindowData windowAtRange(Vector standing, double distance) {
        if(mapBounds.contains(standing)) {
            for(RoomData roomData : rooms) {
                for(WindowData window : roomData.getWindows()) {
                    if(window.inRange(standing, distance)) {
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
            for(RoomData room : rooms) {
                if(room.getBounds().contains(target)) {
                    return room;
                }
            }
        }

        return null;
    }
}