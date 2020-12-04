package io.github.zap.zombies.game.data;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.game.MultiBoundingBox;
import io.github.zap.arenaapi.serialize.DataSerializable;
import io.github.zap.arenaapi.serialize.Serialize;
import io.github.zap.arenaapi.serialize.TypeAlias;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a room.
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@TypeAlias("ZombiesRoom")
public class RoomData extends DataSerializable {
    /**
     * The unique, non-user-friendly name of this room.
     */
    String name;

    /**
     * The message key of the room name
     */
    String roomNameKey;

    /**
     * The bounds of this room, used for knockdown messages and other things
     */
    MultiBoundingBox bounds;

    /**
     * All of the windows contained in this room
     */
    ArrayList<WindowData> windows;

    /**
     * All of the spawnpoints contained in this room
     */
    ArrayList<SpawnpointData> spawnpoints;

    /**
     * Whether or not this room is the 'spawn' room; where the players start off in
     */
    boolean isSpawn;

    /**
     * Arena specific state: whether or not this room has been opened.
     */
    @Serialize(skip = true)
    final Property<Boolean> openProperty = new Property<>(false);

    private RoomData() {}
}