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
import java.util.Set;

/**
 * Represents a gun which shoots a line of particles
 */
public class LinearGun extends Gun<LinearGunData, LinearGunLevel> {

    public LinearGun(Player player, int slotId, LinearGunData equipmentData) {
        super(player, slotId, equipmentData);
    }

    @Override
    public void shoot() {
        Player player = getPlayer();
        World world = player.getWorld();
        Vector eyeLocation = player.getEyeLocation().toVector().clone();
        Vector eyeDirection = player.getEyeLocation().getDirection().clone();
        Vector targetBlockVector = getTargetBlockVector(player);

        sendShot(world, eyeLocation, eyeDirection, targetBlockVector);
    }

    /**
     * Sends the shot
     * @param world The world the player is located in
     * @param particleLocation The start of the shot
     * @param particleDirection The direction of the shot
     * @param targetBlockVector A vector to the targeted block
     */
    private void sendShot(World world, Vector particleLocation, Vector particleDirection, Vector targetBlockVector) {
        LinearBeam beam = new LinearBeam(world, getEquipmentData().getParticle(), particleLocation, particleDirection,
                targetBlockVector, getEquipmentData().getLevels().get(getLevel()).getMaxPierceableEntities());
        beam.send();
    }

    /**
     * Creates a target block vector
     * @param player The player to create the vector for
     * @return The target block vector
     */
    private Vector getTargetBlockVector(Player player) {
        Set<Material> materials = new HashSet<>(){{
           add(Material.AIR);
           add(Material.CAVE_AIR);
           add(Material.VOID_AIR);
        }};

        int range = /* max range */ 50;
        Block targetBlock = player.getTargetBlock(materials, range);
        BoundingBox boundingBox;

        if (materials.contains(targetBlock.getType())) {
            Location location = targetBlock.getLocation();
            boundingBox = new BoundingBox(location.getX(), targetBlock.getY(), targetBlock.getZ(),
                    location.getX() + 1, location.getY() + 1, targetBlock.getZ() + 1);
        } else {
            boundingBox = targetBlock.getBoundingBox();
        }

        return boundingBox.rayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection(),
                range + 1.74).getHitPosition();
    }
}
