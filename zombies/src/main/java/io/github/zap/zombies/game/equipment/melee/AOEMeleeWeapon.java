package io.github.zap.zombies.game.equipment.melee;

import io.github.zap.arenaapi.BukkitTaskManager;
import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.melee.AOEMeleeData;
import io.github.zap.zombies.game.data.equipment.melee.AOEMeleeLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Melee weapon which deals damage with an Area-of-Effect range
 */
public class AOEMeleeWeapon extends MeleeWeapon<@NotNull AOEMeleeData, @NotNull AOEMeleeLevel> {

    private class AOEMeleeDamageAttempt extends MeleeDamageAttempt {

        private final @NotNull Mob mainMob;

        public AOEMeleeDamageAttempt(@NotNull Mob mainMob) {
            this.mainMob = mainMob;
        }

        @Override
        public boolean ignoresArmor(@NotNull Damager damager, @NotNull Mob target) {
            return mainMob.getUniqueId().equals(target.getUniqueId()) && super.ignoresArmor(damager, target);
        }

    }

    private final @NotNull ZombiesArena.DamageHandler damageHandler;

    public AOEMeleeWeapon(@NotNull ZombiesPlayer zombiesPlayer, int slot, @NotNull AOEMeleeData equipmentData,
                          @NotNull BukkitTaskManager taskManager, @NotNull ZombiesArena.DamageHandler damageHandler) {
        super(zombiesPlayer, slot, equipmentData, taskManager);

        this.damageHandler = damageHandler;
    }

    @Override
    public void attack(Mob mob) {
        World world = mob.getWorld();
        Collection<Mob> aoeMobs
                = world.getNearbyEntitiesByType(Mob.class, mob.getLocation(), getCurrentLevel().getRange());

        DamageAttempt damageAttempt = new AOEMeleeDamageAttempt(mob);
        for (Mob otherMob : aoeMobs) {
            damageHandler.damageEntity(getZombiesPlayer(), damageAttempt, otherMob);
        }
    }

}
