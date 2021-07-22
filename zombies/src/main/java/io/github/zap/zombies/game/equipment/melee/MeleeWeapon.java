package io.github.zap.zombies.game.equipment.melee;

import io.github.zap.arenaapi.BukkitTaskManager;
import io.github.zap.zombies.game.DamageAttempt;
import io.github.zap.zombies.game.Damager;
import io.github.zap.zombies.game.data.equipment.melee.MeleeData;
import io.github.zap.zombies.game.data.equipment.melee.MeleeLevel;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a weapon that uses melee combat
 * @param <D> The data type of the weapon
 * @param <L> The level type of the weapon
 */
public abstract class MeleeWeapon<D extends @NotNull MeleeData<L>, @NotNull L extends MeleeLevel>
        extends UpgradeableEquipment<D, L> {

    protected class MeleeDamageAttempt implements DamageAttempt {

        private final @NotNull L meleeLevel = getCurrentLevel();

        private final Player player;

        private final boolean isCritical;

        public MeleeDamageAttempt() {
            player = tryGetPlayer();
            isCritical = player.getFallDistance() > 0F;
        }

        @Override
        public int getCoins(@NotNull Damager damager, @NotNull Mob target) {
            return isCritical ? meleeLevel.getGoldPerCritical() : meleeLevel.getGoldPerHit();
        }

        @Override
        public double damageAmount(@NotNull Damager damager, @NotNull Mob target) {
            return meleeLevel.getDamage();
        }

        @Override
        public boolean ignoresArmor(@NotNull Damager damager, @NotNull Mob target) {
            return isCritical;
        }

        @Override
        public @NotNull Vector directionVector(@NotNull Damager damager, @NotNull Mob target) {
            return target.getLocation().subtract(player.getLocation()).toVector().normalize();
        }

        @Override
        public double knockbackFactor(@NotNull Damager damager, @NotNull Mob target) {
            return meleeLevel.getKnockbackFactor();
        }

    }

    private final @NotNull BukkitTaskManager taskManager;

    private boolean usable = true;

    public MeleeWeapon(@NotNull ZombiesPlayer zombiesPlayer, int slot, @NotNull D equipmentData,
                       @NotNull BukkitTaskManager taskManager) {
        super(zombiesPlayer, slot, equipmentData);

        this.taskManager = taskManager;
    }

    public boolean isUsable() {
        return usable;
    }

    /**
     * Called when the melee weapon is meant to attack a mob
     * @param mob The mob to attack
     */
    public abstract void attack(Mob mob);

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        Player onlinePlayer = tryGetPlayer();
        if (isSelected() && getCurrentLevel().isUsesShields()) {
            EntityEquipment equipment = onlinePlayer.getEquipment();

            if (visible) {
                //noinspection ConstantConditions
                if (equipment.getItemInOffHand().getType() != Material.SHIELD) {
                    equipment.setItemInOffHand(new ItemStack(Material.SHIELD));
                }
            } else {
                //noinspection ConstantConditions
                if (equipment.getItemInOffHand().getType() == Material.SHIELD) {
                    equipment.setItemInOffHand(new ItemStack(Material.AIR));
                }
            }
        }
    }

    @Override
    public void onLeftClick(@NotNull Action action) {
        if (action == Action.LEFT_CLICK_AIR) {
            super.onLeftClick(action);

            if (usable) {
                usable = false;
                taskManager.runTaskLater(getCurrentLevel().getDelayTicks(), () -> usable = true);
            }
        }
    }

    @Override
    public void onSlotSelected() {
        super.onSlotSelected();

        Player onlinePlayer = tryGetPlayer();
        if (getCurrentLevel().isUsesShields()) {
            EntityEquipment equipment = onlinePlayer.getEquipment();

            //noinspection ConstantConditions
            if (equipment.getItemInOffHand().getType() != Material.SHIELD) {
                onlinePlayer.getEquipment().setItemInOffHand(new ItemStack(Material.SHIELD));
            }
        }
    }

    @Override
    public void onSlotDeselected() {
        super.onSlotDeselected();

        Player onlinePlayer = tryGetPlayer();
        if (getCurrentLevel().isUsesShields()) {
            EntityEquipment equipment = onlinePlayer.getEquipment();

            //noinspection ConstantConditions
            if (equipment.getItemInOffHand().getType() == Material.SHIELD) {
                onlinePlayer.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));
            }
        }
    }

}
