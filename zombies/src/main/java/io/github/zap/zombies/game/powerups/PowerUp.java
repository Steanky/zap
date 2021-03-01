package io.github.zap.zombies.game.powerups;

import io.github.zap.zombies.game.ZombiesArena;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public abstract class PowerUp {
    @Getter
    private final PowerUpData data;

    @Getter
    private final ZombiesArena arena;

    @Getter
    private long spawnedTimeStamp;

    protected ArmorStand asItem;
    protected ArmorStand asName;

    public PowerUp(PowerUpData data, ZombiesArena arena) {
        this.data = data;
        this.arena = arena;
    }

    public void spawnItem(Location location) {
        removePowerUpItem();
        asItem = createArmourStand(location);
        //noinspection ConstantConditions
        asItem.getEquipment().setHelmet(new ItemStack(data.getItemRepresentation()), true);
        asName = createArmourStand(location.add(0, 1.25, 0));
        asName.setCustomName(data.getDisplayName());
        asName.setCustomNameVisible(true);
    }

    public void removePowerUpItem() {
        if(asItem != null) {
            asItem.remove();
        }

        if (asName != null) {
            asName.remove();
        }

        spawnedTimeStamp = System.currentTimeMillis();
    }

    public abstract void activate();

    private ArmorStand createArmourStand(Location location) {
        var as = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setMarker(true);
        as.setSmall(true);
        as.setCanTick(false);

        return as;
    }
}
