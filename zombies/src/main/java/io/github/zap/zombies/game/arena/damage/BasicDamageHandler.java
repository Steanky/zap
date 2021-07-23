package io.github.zap.zombies.game.arena.damage;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.arena.spawner.Spawner;
import io.github.zap.zombies.game.data.powerups.DamageModificationPowerUpData;
import io.github.zap.zombies.game.powerups.DamageModificationPowerUp;
import io.github.zap.zombies.game.powerups.PowerUp;
import io.github.zap.zombies.game.powerups.PowerUpState;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * Basic implementation of a {@link DamageHandler}
 */
@SuppressWarnings("ClassCanBeRecord")
public class BasicDamageHandler implements DamageHandler {

    private final @NotNull Spawner spawner;

    private final @NotNull Collection<@NotNull PowerUp> powerUps;

    public BasicDamageHandler(@NotNull Spawner spawner, @NotNull Collection<@NotNull PowerUp> powerUps) {
        this.spawner = spawner;
        this.powerUps = powerUps;
    }

    @Override
    public void damageEntity(@NotNull Damager damager, @NotNull DamageAttempt with, @NotNull Mob target) {
        if (spawner.getMobs().contains(target) && !target.isDead()) {
            target.playEffect(EntityEffect.HURT);

            Optional<ActiveMob> activeMob = MythicMobs.inst().getMobManager().getActiveMob(target.getUniqueId());
            double mobKbFactor = 1;
            if (activeMob.isPresent()) {
                mobKbFactor = activeMob.get().getType().getConfig().getDouble("KnockbackFactor", 1);
            }

            double deltaHealth = inflictDamage(target, with.damageAmount(damager, target), with.ignoresArmor(damager, target));
            Vector resultingVelocity = target.getVelocity().add(with.directionVector(damager, target)
                    .multiply(with.knockbackFactor(damager, target)).multiply(mobKbFactor));

            try {
                target.setVelocity(resultingVelocity);
            }
            catch (IllegalArgumentException ignored) {
                Zombies.warning("Attempted to set velocity for entity " + target.getUniqueId() + " to a vector " +
                        "with a non-finite value " + resultingVelocity);
            }

            damager.onDealsDamage(with, target, deltaHealth);
        }
    }

    private double inflictDamage(Mob mob, double damage, boolean ignoreArmor) {
        boolean instaKill = false;

        for (PowerUp powerUp : powerUps) {
            if (powerUp instanceof DamageModificationPowerUp && powerUp.getState() == PowerUpState.ACTIVATED) {
                var data = (DamageModificationPowerUpData) powerUp.getData();
                if (data.isInstaKill()) {
                    instaKill = true;
                    break;
                }

                damage = damage * data.getMultiplier() + data.getAdditionalDamage();
            }
        }

        double before = mob.getHealth();

        Optional<ActiveMob> activeMob = MythicMobs.inst().getMobManager().getActiveMob(mob.getUniqueId());
        boolean resistInstakill = false;
        if (activeMob.isPresent()) {
            resistInstakill = activeMob.get().getType().getConfig().getBoolean("ResistInstakill", false);
        }

        if (instaKill && !resistInstakill) {
            mob.setHealth(0);
        } else if (ignoreArmor) {
            mob.setHealth(Math.max(mob.getHealth() - damage, 0D));
        } else {
            mob.damage(damage);
        }

        mob.playEffect(EntityEffect.HURT);
        return before - mob.getHealth();
    }

}
