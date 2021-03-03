package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.gun.GunData;
import io.github.zap.zombies.game.data.equipment.gun.GunLevel;
import io.github.zap.zombies.game.equipment.Ultimateable;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents a basic gun
 * @param <D> The gun data type
 * @param <L> The gun level type
 */
@Getter
public abstract class Gun<D extends GunData<L>, L extends GunLevel> extends UpgradeableEquipment<D, L>
        implements Ultimateable {

    private int currentClipAmmo;

    private int currentAmmo;

    private boolean canReload = true;

    private boolean canShoot = true;

    public Gun(ZombiesArena zombiesArena, ZombiesPlayer zombiesPlayer, int slot, D equipmentData) {
        super(zombiesArena, zombiesPlayer, slot, equipmentData);

        refill();
    }

    /**
     * Refills the gun completely
     */
    public void refill() {
        GunLevel gunLevel = getCurrentLevel();
        setAmmo(gunLevel.getAmmo());
        setClipAmmo(gunLevel.getClipAmmo());
    }

    /**
     * Reloads the gun
     */
    public void reload() {
        if (canReload) {
            GunLevel level = getCurrentLevel();
            int clipAmmo = level.getClipAmmo();

            if (currentClipAmmo < clipAmmo && clipAmmo <= currentAmmo) {
                canReload = false;
                Player player = getPlayer();
                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_GALLOP, 1F, 0.5F);

                new BukkitRunnable() {
                    private final int reloadRate = level.getReloadRate();
                    private final int maxVal = getEquipmentData().getMaterial().getMaxDurability();

                    private int step = 0;

                    @Override
                    public void run() {
                        if (step < reloadRate) {
                            setItemDamage(maxVal - (++step * maxVal) / reloadRate);
                        } else {
                            setItemDamage(0);

                            int newClip = Math.min(clipAmmo, currentAmmo);
                            setClipAmmo(newClip);

                            canReload = true;
                            canShoot = true;
                            cancel();
                        }
                    }
                }.runTaskTimer(Zombies.getInstance(), 0L, 1L);
            }
        }
    }

    /**
     * Updates the item stack after shooting the gun
     * (you win the award for the longest method name in the plugin, congratulations) --Steank
     */
    protected void updateRepresentingItemStackAfterShooting() {
        canShoot = false;

        setAmmo(currentAmmo - 1);
        setClipAmmo(currentClipAmmo - 1);

        Player player = getPlayer();
        // Animate xp bar
        new BukkitRunnable() {
            private final int goal =
                    (int) (getCurrentLevel().getFireRate() * getZombiesPlayer().getFireRateMultiplier());
            private final int stepVal = 1 / goal;
            private int step = 0;

            @Override
            public void run() {
                if (step < goal) {
                    step++;
                    if (isSelected()) {
                        player.setExp(++step * stepVal);
                    }
                } else {
                    if (isSelected()) {
                        player.setExp(1);
                    }

                    canShoot = true;
                    cancel();
                }
            }
        }.runTaskTimer(Zombies.getInstance(), 0L, 1L);
        if (currentClipAmmo == 0) {
            if (currentAmmo > 0) {
                reload();
            } else {
                player.sendMessage(ChatColor.RED + "no ammo, bro.");
            }
        }
    }

    /**
     * Utility method to set the item durability
     * @param val the damage value to set
     */
    private void setItemDamage(int val) {
        ItemStack itemStack = getRepresentingItemStack();
        Damageable damageable = (Damageable) itemStack.getItemMeta();
        damageable.setDamage(val);

        getRepresentingItemStack().setItemMeta((ItemMeta) damageable);
        setRepresentingItemStack(getRepresentingItemStack());
    }

    /**
     * Sets the ammo of the weapon
     * @param ammo The new ammo
     */
    protected void setAmmo(int ammo) {
        currentAmmo = ammo;
        if (isVisible()) {
            updateAmmo();
        }
    }

    private void updateAmmo() {
        getPlayer().setLevel(currentAmmo);
    }

    /**
     * Sets the clip ammo of the weapon
     * @param clipAmmo The new clip ammo
     */
    protected void setClipAmmo(int clipAmmo) {
        this.currentClipAmmo = clipAmmo;

        if (isVisible()) {
            updateClipAmmo();
        }
    }

    private void updateClipAmmo() {
        if (currentClipAmmo > 0) {
            setItemDamage(0);
            getRepresentingItemStack().setAmount(currentClipAmmo);
        } else {
            setItemDamage(getEquipmentData().getMaterial().getMaxDurability());
            getRepresentingItemStack().setAmount(1);
        }
        setRepresentingItemStack(getRepresentingItemStack());

        // TODO: work around for item not updating meta twice in 1 server thread iteration
        getPlayer().updateInventory();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            updateAmmo();
            updateClipAmmo();
        }

        super.setVisible(visible);
    }

    @Override
    public void onSlotSelected() {
        super.onSlotSelected();
        Player player = getPlayer();

        player.setLevel(currentAmmo);
        player.setExp(1);
    }

    @Override
    public void onSlotDeselected() {
        super.onSlotDeselected();
        Player player = getPlayer();

        player.setLevel(0);
        player.setExp(0);
    }

    @Override
    public void onLeftClick() {
        super.onLeftClick();

        reload();
    }

    @Override
    public void onRightClick() {
        super.onRightClick();

        if (canShoot) {
            shoot();
            updateRepresentingItemStackAfterShooting();
        }
    }

    /**
     * Shoots the gun
     */
    public abstract void shoot();

}
