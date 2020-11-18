package io.github.zap.zombies.game.data;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.Unique;
import io.github.zap.arenaapi.game.MultiBoundingBox;
import io.github.zap.arenaapi.serialize.DataSerializable;
import io.github.zap.arenaapi.serialize.Serialize;
import io.github.zap.arenaapi.serialize.TypeAlias;
import io.github.zap.arenaapi.util.VectorUtils;
import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Represents a window.
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@TypeAlias(alias = "ZombiesWindow")
public class WindowData extends DataSerializable {
    /**
     * The materials that should be used to repair this window. Each index corresponds to the coordinate located at
     * the same index in faceVectors.
     */
    List<Material> repairedMaterials;

    /**
     * Works exactly the same as repairedMaterials, but these materials are used during window breaking. Might remove
     * this at a later date as I'm not exactly sure of its utility
     */
    List<Material> brokenMaterials;

    /**
     * A list of vectors corresponding to the blocks of window face
     */
    List<Vector> faceVectors;

    /**
     * A BoundingBox containing the face of the window
     */
    BoundingBox faceBounds;

    /**
     * The bounds of the window interior - used for player position checking
     */
    MultiBoundingBox interiorBounds;

    /**
     * The coordinate considered the 'base' of the window, to which players are teleported if they enter the interior
     */
    Vector base;

    /**
     * The center of the window's face, used for distance checking. This value is calculated once and cached.
     */
    @Serialize(skip = true)
    Vector center;

    /**
     * The volume of the window's face. This is calculated once and cached.
     */
    @Serialize(skip = true)
    int volume = -1;

    /**
     * Arena specific state: the current index at which the window is being repaired or broken. This points to the index
     * of the current repaired block; thus, if the window is fully broken, it will == -1
     */
    @Serialize(skip = true)
    final Property<Integer> currentIndexAccessor = new Property<>(getVolume() - 1);

    /**
     * Arena specific state: the player who is currently repairing the window
     */
    @Serialize(skip = true)
    final Property<ZombiesPlayer> repairingPlayer = new Property<>(null);

    /**
     * Arena specific state: the entity that is currently attacking the window
     */
    @Serialize(skip = true)
    final Property<Entity> attackingEntity = new Property<>(null);

    private WindowData() {}

    /**
     * Gets the center of the window's face (its breakable/repairable blocks)
     * @return The central vector of the window's face
     */
    public Vector getCenter() {
        if(center == null) {
            center = faceBounds.getCenter();
        }

        return center.clone();
    }

    /**
     * Gets the volume of the window's face (its breakable/repairable blocks)
     * @return The volume of the window's face
     */
    public int getVolume() {
        if(volume == -1) {
            volume = (int)faceBounds.getVolume();
        }

        return volume;
    }

    /**
     * Performs a range check on the window.
     * @param standing The vector to base the check from
     * @param distance The distance to base the range check off of
     * @return Whether or not the window is within the specified distance from the standing vector
     */
    public boolean inRange(Vector standing, double distance) {
        return VectorUtils.manhattanDistance(standing, getCenter()) < distance;
    }

    /**
     * Incrementally repairs this window by the specified amount. The repair index is limited by the volume of the
     * window.
     * @param accessor The accessor using this object
     * @param by The amount to try to advance the repair index by
     * @return The number of blocks that were actually repaired
     */
    public int advanceRepairState(Unique accessor, int by) {
        int currentIndex = currentIndexAccessor.get(accessor);
        int max = getVolume() - 1;

        if(currentIndex < max) {
            int repaired = Math.min(currentIndex + by, max);
            currentIndexAccessor.set(accessor, repaired);
            return repaired;
        }

        return 0;
    }

    /**
     * Incrementally breaks the window by the specified amount. Works the same as advanceRepairState, but in reverse.
     * @param accessor The accessor using this object
     * @param by The amount to reduce the repair index by
     * @return true if any number of breaks occurred, false otherwise
     */
    public boolean retractRepairState(Unique accessor, int by) {
        int currentIndex = currentIndexAccessor.get(accessor);

        if(currentIndex > -1) {
            currentIndexAccessor.set(accessor, Math.max(currentIndex - by, -1));
            return true;
        }

        return false;
    }
}