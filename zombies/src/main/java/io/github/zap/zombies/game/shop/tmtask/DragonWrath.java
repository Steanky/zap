package io.github.zap.zombies.game.shop.tmtask;

import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.github.zap.zombies.game.shop.TeamMachine;
import io.github.zap.zombies.game.data.shop.tmtask.DragonWrathData;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

/**
 * Task which kills all zombies within a certain radius of the activating player
 */
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class DragonWrath extends TeamMachineTask<@NotNull DragonWrathData> implements Damager {

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

    public DragonWrath(DragonWrathData dragonWrathData) {
        super(dragonWrathData);
    }

    @Override
    public boolean execute(TeamMachine teamMachine, ZombiesArena arena, ZombiesPlayer zombiesPlayer) {
        if (super.execute(teamMachine, zombiesArena, zombiesPlayer)) {
            Location location = teamMachine.getBlock().getLocation();
            Set<UUID> mobIds = zombiesArena.getEntitySet();

            World world = zombiesArena.getWorld();
            world.playSound(Sound.sound(Key.key("minecraft:entity.ender_dragon.growl"), Sound.Source.MASTER,
                    1.0F, 1.0F), location.getX(), location.getY(), location.getZ());

            zombiesArena.runTaskLater(delay, () -> {
                int entitiesKilled = 0;
                for (Mob mob : world.getNearbyEntitiesByType(Mob.class, location, radius)) {
                    if (mobIds.contains(mob.getUniqueId())) {
                        world.strikeLightningEffect(mob.getLocation());
                        zombiesArena.getDamageHandler().damageEntity(
                                DragonWrath.this,
                                new DragonWrathDamage(),
                                mob
                        );
                        entitiesKilled++;
                    }
                }

                zombiesPlayer.addKills(entitiesKilled);

                zombiesPlayer.getPlayer().sendMessage(Component.text(String.format("Killed %d mobs!", entitiesKilled),
                        NamedTextColor.GREEN));
            });

            return true;
        }

        return false;
    }

    @Override
    public int getCost() {
        return getTeamMachineTaskData().getInitialCost()
                + (getTeamMachineTaskData().getCostIncrement() * getTimesUsed());
    }

}
