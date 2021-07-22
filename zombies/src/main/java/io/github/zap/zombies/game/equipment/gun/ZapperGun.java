package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.arenaapi.BukkitTaskManager;
import io.github.zap.arenaapi.stats.StatsManager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.gun.ZapperGunData;
import io.github.zap.zombies.game.data.equipment.gun.ZapperGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.equipment.gun.logic.ZapperBeam;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a gun that zaps entities
 */
public class ZapperGun extends Gun<@NotNull ZapperGunData, @NotNull ZapperGunLevel> {

    private final @NotNull MapData map;

    public ZapperGun(@NotNull ZombiesPlayer zombiesPlayer, int slot, @NotNull ZapperGunData equipmentData,
                     @NotNull StatsManager statsManager, @NotNull BukkitTaskManager taskManager, @NotNull MapData map) {
        super(zombiesPlayer, slot, equipmentData, statsManager, taskManager);

        this.map = map;
    }

    @Override
    public void shoot() {
        ZapperGunData zapperGunData = getEquipmentData();
        ZapperGunLevel currentLevel = zapperGunData.getLevels().get(getLevel());

        new ZapperBeam(map, getZombiesPlayer(), tryGetPlayer().getEyeLocation(), currentLevel,
                zapperGunData.getParticle(), zapperGunData.getParticleDataWrapper()).send();
    }

}
