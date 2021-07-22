package io.github.zap.zombies.game.equipment.melee;

import io.github.zap.arenaapi.BukkitTaskManager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.melee.BasicMeleeData;
import io.github.zap.zombies.game.data.equipment.melee.BasicMeleeLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * A basic melee weapon implementation
 */
public class BasicMeleeWeapon extends MeleeWeapon<@NotNull BasicMeleeData, @NotNull BasicMeleeLevel> {

    private final @NotNull ZombiesArena.DamageHandler damageHandler;

    public BasicMeleeWeapon(@NotNull ZombiesPlayer zombiesPlayer, int slot, @NotNull BasicMeleeData equipmentData,
                            @NotNull BukkitTaskManager taskManager, @NotNull ZombiesArena.DamageHandler damageHandler) {
        super(zombiesPlayer, slot, equipmentData, taskManager);

        this.damageHandler = damageHandler;
    }

    @Override
    public void attack(Mob mob) {
        damageHandler.damageEntity(getZombiesPlayer(), new MeleeDamageAttempt(), mob);
    }

}
