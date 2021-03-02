package io.github.zap.zombies.game.powerups;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.powerups.events.ChangedAction;
import io.github.zap.zombies.game.powerups.events.PowerUpChangedEventArgs;
import lombok.Getter;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;

public abstract class PowerUp {
    @Getter
    private final PowerUpData data;

    @Getter
    private final ZombiesArena arena;

    @Getter
    private final int refreshRate;

    @Getter
    private long spawnedTimeStamp;

    @Getter
    private PowerUpState state = PowerUpState.NONE;

    @Getter
    private long activatedTimeStamp;

    private Location powerUpItemLocation;
    private BukkitTask checkForDistTask;

    protected ArmorStand asItem;
    protected ArmorStand asName;

    public PowerUp(PowerUpData data, ZombiesArena arena) {
        this(data, arena, 10);
    }

    public PowerUp(PowerUpData data, ZombiesArena arena, int refreshRate) {
        this.data = data;
        this.arena = arena;
        this.refreshRate = refreshRate;
    }

    public void spawnItem(Location location) {
        if(state != PowerUpState.NONE)
            throw new IllegalStateException("Cannot summon item in this state!");

        state = PowerUpState.DROPPED;
        removePowerUpItem();
        asItem = createArmourStand(location);
        //noinspection ConstantConditions
        asItem.getEquipment().setHelmet(new ItemStack(getData().getItemRepresentation()), true);
        asName = createArmourStand(location.add(0, 1.25, 0));
        asName.setCustomName(data.getDisplayName());
        asName.setCustomNameVisible(true);

        // Check distance & time-out
        powerUpItemLocation = location;
        checkForDistTask = new BukkitRunnable() {
            @Override
            public void run() {
                MutableBoolean isPickedUp = new MutableBoolean(false);

                getArena().getPlayerMap().forEach((l,r) -> {
                    var dist = r.getPlayer().getLocation().distance(powerUpItemLocation);
                    var pickupDist = getData().getPickupRange();
                    if(dist <= pickupDist && !(boolean)isPickedUp.getValue() && getState() == PowerUpState.DROPPED) {
                        if(!checkForDistTask.isCancelled()) checkForDistTask.cancel();
                        isPickedUp.setValue(true);
                        removePowerUpItem();
                        var eventArgs = new PowerUpChangedEventArgs(ChangedAction.ACTIVATED, Collections.singleton(getCurrent()));
                        getArena().getPowerUpChangedEvent().callEvent(eventArgs);
                        getArena().getPlayerMap().forEach((id,player) -> {
                            player.getPlayer().sendTitle(getData().getDisplayName(), "");
                            player.getPlayer().sendMessage(ChatColor.YELLOW +  r.getPlayer().getName() + " activated " + getData().getDisplayName());
                            player.getPlayer().playSound(player.getPlayer().getLocation(), getData().getPickupSound(), getData().getPickupSoundVolume(), getData().getPickupSoundPitch());
                        });
                        state = PowerUpState.ACTIVATED;
                        activatedTimeStamp = System.currentTimeMillis();
                        activate();
                    }
                });
            }
        }.runTaskTimer(Zombies.getInstance(), 0, getRefreshRate());
    }

    public void removePowerUpItem() {
        if(asItem != null) {
            asItem.remove();
        }

        if (asName != null) {
            asName.remove();
        }

        // TODO: Despawn
        spawnedTimeStamp = System.currentTimeMillis();
    }

    public abstract void activate();

    public  void deactivate() {
        if(arena.getPowerUps().contains(this)) {
            getArena().getPowerUps().remove(this);
            var eventArgs = new PowerUpChangedEventArgs(ChangedAction.REMOVE, Collections.singleton(this));
            getArena().getPowerUpChangedEvent().callEvent(eventArgs);
            state = PowerUpState.REMOVED;
        }
    }

    private ArmorStand createArmourStand(Location location) {
        var as = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setMarker(true);
        as.setSmall(true);
        as.setCanTick(false);

        return as;
    }

    // For BukkitRunnable to retrieve
    private PowerUp getCurrent() {
        return this;
    }
}
