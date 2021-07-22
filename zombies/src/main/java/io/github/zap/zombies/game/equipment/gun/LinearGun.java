package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.arenaapi.BukkitTaskManager;
import io.github.zap.arenaapi.stats.StatsManager;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunData;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.equipment.gun.logic.LinearBeam;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a gun which shoots a line of particles and damages guns within a line
 */
public class LinearGun extends Gun<@NotNull LinearGunData, @NotNull LinearGunLevel> {

    private final @NotNull MapData map;

    public LinearGun(@NotNull ZombiesPlayer zombiesPlayer, int slot, @NotNull LinearGunData equipmentData,
                     @NotNull StatsManager statsManager, @NotNull BukkitTaskManager taskManager, @NotNull MapData map) {
        super(zombiesPlayer, slot, equipmentData, statsManager, taskManager);

        this.map = map;
    }

    @Override
    public void shoot() {
        LinearGunData linearGunData = getEquipmentData();
        LinearGunLevel currentLevel = getCurrentLevel();

        new LinearBeam(map, getZombiesPlayer(), tryGetPlayer().getEyeLocation(), currentLevel,
                linearGunData.getParticle(), linearGunData.getParticleDataWrapper()).send();
    }

}
