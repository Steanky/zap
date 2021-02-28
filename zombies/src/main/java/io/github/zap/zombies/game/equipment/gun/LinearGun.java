package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunData;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import io.github.zap.zombies.game.equipment.gun.logic.LinearBeam;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a gun which shoots a line of particles and damages guns within a line
 */
public class LinearGun extends Gun<LinearGunData, LinearGunLevel> {

    public LinearGun(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, LinearGunData equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);
    }

    @Override
    public void shoot() {
        LinearGunData linearGunData = getEquipmentData();
        LinearGunLevel currentLevel = linearGunData.getLevels().get(getLevel());

        new LinearBeam(
                getZombiesArena().getMap(),
                getPlayer().getEyeLocation(),
                linearGunData.getParticle(),
                currentLevel
        ).send();
    }
}
