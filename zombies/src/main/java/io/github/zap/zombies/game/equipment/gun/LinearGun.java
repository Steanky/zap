package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.zombies.game.data.equipment.gun.LinearGunData;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import io.github.zap.zombies.game.equipment.gun.logic.LinearBeam;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a gun which shoots a line of particles
 */
public class LinearGun extends Gun<LinearGunData, LinearGunLevel> {

    private static final Set<Material> AIR_MATERIALS = new HashSet<>(){{
        add(Material.AIR);
        add(Material.CAVE_AIR);
        add(Material.VOID_AIR);
    }};

    public LinearGun(Player player, int slot, LinearGunData equipmentData) {
        super(player, slot, equipmentData);
    }

    @Override
    public void shoot() {
        Player player = getPlayer();
        World world = player.getWorld();

        Location eyeLocation = player.getEyeLocation();
        Vector eyeDirection = player.getEyeLocation().getDirection();
        Vector targetBlockVector = getTargetBlockVector(player);

        sendShot(world, eyeLocation.clone(), eyeDirection, targetBlockVector);
    }

    /**
     * Sends the shot
     * @param world The world the player is located in
     * @param eyeLocation The start of the shot
     * @param particleDirection The direction of the shot
     * @param targetBlockVector A vector to the targeted block
     */
    private void sendShot(World world, Location eyeLocation, Vector particleDirection, Vector targetBlockVector) {
        LinearGunData linearGunData = getEquipmentData();

        new LinearBeam(
                world,
                eyeLocation,
                particleDirection,
                eyeLocation.toVector().distance(targetBlockVector),
                linearGunData.getParticle(),
                linearGunData.getMaxPierceableEntities(),
                getCurrentLevel().getDamage()
        ).send();
    }

    /**
     * Creates a target block vector
     * @param player The player to create the vector for
     * @return The target block vector
     */
    private Vector getTargetBlockVector(Player player) {
        int range = getCurrentLevel().getRange();
        Block targetBlock = player.getTargetBlock(AIR_MATERIALS, range);
        BoundingBox boundingBox;

        if (AIR_MATERIALS.contains(targetBlock.getType())) {
            Location location = targetBlock.getLocation();
            boundingBox = new BoundingBox(location.getX(), targetBlock.getY(), targetBlock.getZ(),
                    location.getX() + 1, location.getY() + 1, targetBlock.getZ() + 1);
        } else {
            boundingBox = targetBlock.getBoundingBox();
        }

        return Objects.requireNonNull(
                boundingBox.rayTrace(
                        player.getEyeLocation().toVector(),
                        player.getEyeLocation().getDirection(),
                range + 1.74)
        ).getHitPosition();
    }
}
