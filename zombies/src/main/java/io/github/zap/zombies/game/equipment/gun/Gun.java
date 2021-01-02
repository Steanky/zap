package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.equipment.gun.GunData;
import io.github.zap.zombies.game.data.equipment.gun.GunLevel;
import io.github.zap.zombies.game.equipment.Ultimateable;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import lombok.Getter;
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

    public Gun(Player player, int slot, D equipmentData) {
        super(player, slot, equipmentData);
    }

    /**
     * Refills the gun completely
     */
    public void refill() {
        GunLevel gunLevel = getEquipmentData().getLevels().get(getLevel());
        setAmmo(gunLevel.getAmmo());
        setClipAmmo(gunLevel.getClipAmmo());
    }

    /**
     * Reloads the gun
     */
    public void reload() {
        if (canReload) {
            GunLevel level = getEquipmentData().getLevels().get(getLevel());
            int clipAmmo = level.getClipAmmo();

            if (currentClipAmmo < clipAmmo && clipAmmo <= currentAmmo) {
                canReload = false;
                getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_HORSE_GALLOP, 1F, 0.5F);

                new BukkitRunnable() {
                    private final float reloadRate = level.getReloadRate();
                    private final int maxVal = getEquipmentData().getMaterial().getMaxDurability();
                    private final float stepVal = maxVal / (reloadRate * 20);

                    private int step = 0;

                    @Override
                    public void run() {
                        if(step < (int) (reloadRate * 20)) {
                            setItemDamage((int)(maxVal - (step + 1) * stepVal));
                            step++;
                        } else {
                            setItemDamage(0);
                            setClipAmmo(Math.min(clipAmmo, currentAmmo));

                            canReload = true;
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

        if (currentClipAmmo > 0) {
            // Animate xp bar
            new BukkitRunnable() {
                private final float fireRate = getEquipmentData().getLevels().get(getLevel()).getFireRate();
                private final float goal = fireRate * 20;
                private final float stepVal = 1 / (fireRate * 20);

                private int step = 0;


                @Override
                public void run() {
                    if(step < goal && isSelected()) {
                        getPlayer().setExp((step + 1) * stepVal);
                        step++;
                    } else {
                        if (isSelected()) {
                            getPlayer().setExp(1);
                        }

                        cancel();
                    }
                }
            }.runTaskTimer(Zombies.getInstance(), 0L, 1L);
        } else {
            if (currentAmmo > 0) {
                reload();
            } else {
                // TODO: not enough ammo!
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
    }

    /**
     * Sets the ammo of the weapon
     * @param ammo The new ammo
     */
    protected void setAmmo(int ammo) {
        if (isVisible()) {
            currentAmmo = ammo;
            getPlayer().setLevel(ammo);
        }
    }

    /**
     * Sets the clip ammo of the weapon
     * @param clipAmmo The new clip ammo
     */
    protected void setClipAmmo(int clipAmmo) {
        if (isVisible()) {
            this.currentClipAmmo = clipAmmo;

            if (clipAmmo > 0) {
                setItemDamage(0);
                getRepresentingItemStack().setAmount(clipAmmo);
            } else {
                setItemDamage(getEquipmentData().getMaterial().getMaxDurability());
                getRepresentingItemStack().setAmount(1);
            }

            // TODO: work around for item not updating meta twice in 1 server thread iteration
            getPlayer().updateInventory();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

    @Override
    public void onSlotSelected() {
        getPlayer().setLevel(currentAmmo);
        getPlayer().setExp(1);
    }

    @Override
    public void onSlotDeselected() {
        super.onSlotDeselected();

        getPlayer().setLevel(0);
        getPlayer().setExp(0);
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
