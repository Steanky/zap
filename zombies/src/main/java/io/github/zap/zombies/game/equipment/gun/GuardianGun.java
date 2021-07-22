package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.arenaapi.BukkitTaskManager;
import io.github.zap.arenaapi.stats.StatsManager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.gun.GuardianGunData;
import io.github.zap.zombies.game.data.equipment.gun.GuardianGunLevel;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.equipment.gun.logic.GuardianBeam;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

public class GuardianGun extends Gun<@NotNull GuardianGunData, @NotNull GuardianGunLevel> {

    private final @NotNull MapData map;

    public GuardianGun(@NotNull ZombiesPlayer player, int slot, @NotNull GuardianGunData equipmentData,
                       @NotNull StatsManager statsManager, @NotNull BukkitTaskManager taskManager,
                       @NotNull MapData map) {
        super(player, slot, equipmentData, statsManager, taskManager);

        this.map = map;
    }

    @Override
    public void shoot() {
        GuardianGunLevel currentLevel = getCurrentLevel();

        new GuardianBeam(map, getZombiesPlayer(), tryGetPlayer().getEyeLocation(), currentLevel).send();
    }
}
