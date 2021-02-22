package io.github.zap.zombies.game.data.map;

import io.github.zap.arenaapi.Property;
import io.github.zap.arenaapi.Unique;
import io.github.zap.arenaapi.game.MultiBoundingBox;
import io.github.zap.arenaapi.util.VectorUtils;
import io.github.zap.zombies.game.ZombiesPlayer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a window.
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WindowData {
    /**
     * The materials that should be used to repair this window. Each index corresponds to the coordinate located at
     * the same index in faceVectors.
     */
    List<Material> repairedMaterials = new ArrayList<>();

    /**
     * A list of vectors corresponding to the blocks of window face
     */
    List<Vector> faceVectors = new ArrayList<>();

    /**
     * A BoundingBox containing the face of the window
     */
    BoundingBox faceBounds = new BoundingBox();

    /**
     * The bounds of the window interior - used for player position checking
     */
    MultiBoundingBox interiorBounds = new MultiBoundingBox();

    /**
     * The coordinate considered the 'base' of the window, to which players are teleported if they enter the interior
     */
    Vector base = new Vector();

    /**
     * The center of the window's face, used for distance checking. This value is calculated once and cached.
     */
    Vector center = new Vector();

    /**
     * The volume of the window's face. This is calculated once and cached.
     */
    int volume = -1;

    /**
     * Arena specific state: the current index at which the window is being repaired or broken. This points to the index
     * of the current repaired block; thus, if the window is fully broken, it will == -1
     */
    transient final Property<Integer> currentIndexProperty = new Property<>(getVolume() - 1);

    /**
     * Arena specific state: the player who is currently repairing the window
     */
    transient final Property<ZombiesPlayer> repairingPlayerProperty = new Property<>(null);

    /**
     * Arena specific state: the entity that is currently attacking the window
     */
    transient final Property<Entity> attackingEntityProperty = new Property<>(null);

    private WindowData() {}

    public WindowData(World from, BoundingBox faceBounds) {
        this.faceBounds = faceBounds;

        Vector min = faceBounds.getMin();
        Vector max = faceBounds.getMax();

        for(int x = min.getBlockX(); x < max.getBlockX(); x++) {
            for(int y = min.getBlockY(); y < max.getBlockY(); y++) {
                for(int z = min.getBlockZ(); z < max.getBlockZ(); z++) {
                    repairedMaterials.add(from.getBlockAt(x, y, z).getType());
                    faceVectors.add(new Vector(x, y, z));
                }
            }
        }
    }

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
        int currentIndex = currentIndexProperty.getValue(accessor);
        int max = getVolume() - 1;

        if(currentIndex < max) {
            int repaired = Math.min(currentIndex + by, max);
            currentIndexProperty.setValue(accessor, repaired);
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
        int currentIndex = currentIndexProperty.getValue(accessor);

        if(currentIndex > -1) {
            currentIndexProperty.setValue(accessor, Math.max(currentIndex - by, -1));
            return true;
        }

        return false;
    }
}
