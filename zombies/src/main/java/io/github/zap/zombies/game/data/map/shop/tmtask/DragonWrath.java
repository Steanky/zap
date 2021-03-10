package io.github.zap.zombies.game.data.map.shop.tmtask;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

/**
 * Task which kills all zombies within a certain radius of the activating player
 */
public class DragonWrath extends TeamMachineTask implements Damager {
    private static class DragonWrathDamage implements DamageAttempt {
        @Override
        public int getCoins(@NotNull Damager damager, @NotNull Mob target) {
            return 0;
        }

        @Override
        public double damageAmount(@NotNull Damager damager, @NotNull Mob target) {
            return target.getHealth();
        }

        @Override
        public boolean ignoresArmor(@NotNull Damager damager, @NotNull Mob target) {
            return true;
        }

        @Override
        public @NotNull Vector directionVector(@NotNull Damager damager, @NotNull Mob target) {
            return new Vector();
        }

        @Override
        public double knockbackFactor(@NotNull Damager damager, @NotNull Mob target) {
            return 0;
        }
    }

    private int costIncrement;

    private int delay;

    private int radius;

    public DragonWrath() {
        super(TeamMachineTaskType.DRAGON_WRATH.name());
    }

    @Override
    public boolean execute(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer) {
        if (super.execute(zombiesArena, zombiesPlayer)) {
            Location location = zombiesPlayer.getPlayer().getLocation();
            Set<UUID> mobIds = zombiesArena.getMobs();

            World world = zombiesArena.getWorld();
            world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 1.0F);

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Mob mob : world.getNearbyEntitiesByType(Mob.class, location, radius)) {
                        if (mobIds.contains(mob.getUniqueId())) {
                            world.strikeLightningEffect(mob.getLocation());
                            zombiesArena.getDamageHandler().damageEntity(DragonWrath.this, new DragonWrathDamage(), mob);
                        }
                    }
                }
            }.runTaskLater(Zombies.getInstance(), 20L * delay);

            return true;
        }

        return false;
    }

    @Override
    public int getCost() {
        return getInitialCost() + (costIncrement * getTimesUsed());
    }

    @Override
    public void onDealsDamage(@NotNull DamageAttempt item, @NotNull Mob damaged, double deltaHealth) { }
}
