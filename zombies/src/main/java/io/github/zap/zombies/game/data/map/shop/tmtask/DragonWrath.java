package io.github.zap.zombies.game.data.map.shop.tmtask;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.shop.TeamMachine;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Task which kills all zombies within a certain radius of the activating player
 */
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
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

    private int costIncrement = 1000;

    private int delay = 30;

    private double radius = 15D;

    public DragonWrath() {
        super(TeamMachineTaskType.DRAGON_WRATH.name());
    }

    @Override
    public boolean execute(TeamMachine teamMachine, ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer) {
        if (super.execute(teamMachine, zombiesArena, zombiesPlayer)) {
            Location location = teamMachine.getBlock().getLocation();
            Set<UUID> mobIds = zombiesArena.getMobs();

            World world = zombiesArena.getWorld();
            world.playSound(Sound.sound(
                    Key.key("minecraft:entity.ender_dragon.growl"),
                    Sound.Source.MASTER,
                    1.0F,
                    1.0F
            ), location.getX(), location.getY(), location.getZ());

            new BukkitRunnable() {
                @Override
                public void run() {
                    Collection<Mob> mobs = world.getNearbyEntitiesByType(Mob.class, location, radius);

                    for (Mob mob : mobs) {
                        if (mobIds.contains(mob.getUniqueId())) {
                            world.strikeLightningEffect(mob.getLocation());
                            zombiesArena.getDamageHandler()
                                    .damageEntity(DragonWrath.this, new DragonWrathDamage(), mob);
                        }
                    }

                    zombiesPlayer.getPlayer().sendMessage(
                            Component.text(String.format("Killed %d mobs!", mobs.size())).color(NamedTextColor.GREEN)
                    );
                }
            }.runTaskLater(Zombies.getInstance(), delay);

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
