package io.github.zap.zombies.game.data.map;

import io.github.zap.arenaapi.Property;
import io.github.zap.zombies.game.data.map.shop.DoorData;
import io.github.zap.zombies.game.data.map.shop.ShopData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * This class represents a Zombies map. It is effectively a pure data class; it only contains helper functions for
 * retrieving and manipulating its own data values.
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MapData {
    /**
     * The unique name of this map that need not be user friendly
     */
    String name;

    /**
     * The resource key of the map name
     */
    String mapNameKey;

    /**
     * The name of the world corresponding to this map
     */
    String worldName;

    /**
     * The bounds of the map, inside which every component should exist
     */
    BoundingBox mapBounds = new BoundingBox();

    /**
     * The spawn vector of this map.
     */
    Vector spawn = new Vector();

    /**
     * The minimum required number of players that this map can start with
     */
    int minimumCapacity = 4;

    /**
     * The maximum number of players this map can hold
     */
    int maximumCapacity = 4;

    /**
     * The duration of the game start countdown timer, in seconds
     */
    int countdownSeconds = 20;

    /**
     * The number of coins each player should start with
     */
    int startingCoins = 0;

    /**
     * The number of coins you get for repairing a window
     */
    int coinsOnRepair = 20;

    /**
     * Whether or not spectators are allowed here
     */
    boolean spectatorAllowed = true;

    /**
     * If this is true, players will be required to be holding nothing in order to open doors
     */
    boolean handRequiredToOpenDoors = false;

    /**
     * Whether or not the players should be allowed to forcibly start the game regardless of the minimum player limit
     */
    boolean forceStart = false;

    /**
     * Whether this map allows players to rejoin after the game has started
     */
    boolean allowRejoin = true;

    /**
     * The minimum (Manhattan) distance in blocks that players must be from a window in order to repair it
     */
    int windowRepairRadiusSquared = 16;

    /**
     * The base delay, in Minecraft server ticks (20ths of a second) that occurs between window blocks being repaired
     */
    int windowRepairTicks = 20;

    /**
     * The time it takes, in Minecraft server ticks, for a corpse to die and for players to no longer be able to revive
     * the corpse
     */
    int corpseDeathTime;

    /**
     * The minimum distance in blocks that players must be from a player corpse in order to revive it
     */
    int reviveRadius = 3;

    /**
     * The MythicMobs mob level that mobs will spawn at
     */
    int mobSpawnLevel = 1;

    /**
     * The material that should replace door blocks when they are opened.
     */
    Material doorFillMaterial = Material.AIR;

    //perk stuff below

    /**
     * Whether or not perks should be lost when a player quits the game.
     */
    boolean perksLostOnQuit = false;

    /**
     * The maximum level of the speed perk (how many times it can be bought)
     */
    int speedMaxLevel = 1;

    /**
     * The strength of the speed effect given by the speed perk.
     */
    int speedAmplifier = 2;

    /**
     * The duration of the effect given by the speed perk.
     */
    int speedDuration = 500;

    /**
     * The interval at which speed from the speed perk is applied to the player.
     */
    int speedReapplyInterval = 500;

    /**
     * Gets the maximum quick fire level supported by this map.
     */
    int quickFireMaxLevel = 1;

    /**
     * The amount of ticks subtracted from base weapon fire delay when quick fire is active. Actual value scales
     * according to xy, where x is the delay reduction and y is the number of levels.
     */
    int quickFireDelayReduction = 5;

    /**
     * The maximum level of extra health
     */
    int extraHealthMaxLevel = 1;

    /**
     * The amount of HP extra health grants, per level
     */
    int extraHealthHpPerLevel = 10;

    /**
     * The maximum level of extra weapon
     */
    int extraWeaponMaxLevel = 1;

    /**
     * The maximum level of fast revive
     */
    int fastReviveMaxLevel = 1;

    /**
     * The default number of ticks it takes for a player to be revived
     */
    int defaultReviveTime;

    /**
     * The amount of ticks subtracted from the revive time per each level
     */
    int tickReductionPerLevel = 10;

    /**
     * Number of rolls before a chest moves to a new location
     */
    int rollsPerChest = 5;

    /**
     * The list of rooms managed by this map
     */
    List<RoomData> rooms = new ArrayList<>();

    /**
     * All doors in this map
     */
    List<DoorData> doors = new ArrayList<>();

    /**
     * All the shops managed by this map
     */
    List<ShopData> shops = new ArrayList<>();

    /**
     * All the rounds in the game
     */
    List<RoundData> rounds = new ArrayList<>();

    /**
     * Map of hotbar object group names to the slots allocated for them
     */
    Map<String, Set<Integer>> hotbarObjectGroupSlots = new HashMap<>();

    /**
     * Equipments given at the start of the game by their name
     */
    List<String> defaultEquipments = new ArrayList<>();

    double fireRatePerkMultiplier = 0.25;

    /**
     * A map of SpawnRule objects which are used to define the behavior of spawnpoints.
     */
    Map<String, SpawnRule> spawnRules = new HashMap<>();

    transient final Property<Integer> currentRoundProperty = new Property<>(0);

    private MapData() {}

    public MapData(String mapName, String worldName, BoundingBox bounds) {
        this.name = mapName;
        this.worldName = worldName;
        this.mapNameKey = String.format("map.%s.name", mapName);
        this.mapBounds = bounds;
    }

    /**
     * Gets the window whose face contains the provided vector, or null if the vector is not inside any windows.
     * @param target The vector that may be inside of a window
     * @return The window the vector is in, or null if it's not inside anything
     */
    public WindowData windowAt(Vector target) {
        if(mapBounds.contains(target)) {
            for(RoomData roomData : rooms) {
                if(roomData.getBounds().contains(target)) { //optimization: don't iterate through rooms we don't need to
                    for(WindowData window : roomData.getWindows()) {
                        if(window.getFaceBounds().contains(target)) {
                            return window;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets the window that may be within range of the specified vector.
     * @param standing The vector used as the origin for the distance check
     * @return The WindowData, or null if there is none in range
     */
    public WindowData windowAtRange(Vector standing, double distanceSquared) {
        if(mapBounds.contains(standing)) {
            for(RoomData roomData : rooms) {
                for(WindowData window : roomData.getWindows()) {
                    if(window.inRange(standing, distanceSquared)) {
                        return window;
                    }
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