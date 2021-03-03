package io.github.zap.zombies.game.powerups;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.powerups.PowerUpData;
import io.github.zap.zombies.game.powerups.events.ChangedAction;
import io.github.zap.zombies.game.powerups.events.PowerUpChangedEventArgs;
import lombok.Getter;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Collections;

public abstract class PowerUp {
    public static float ITEM_SPIN_ANGULAR_VELOCITY = 180; // deg/s

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

    protected Item itemEntity;
    protected ArmorStand asName;

    public PowerUp(PowerUpData data, ZombiesArena arena) {
        this(data, arena, 1);
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
        itemEntity = (Item) location.getWorld().spawnEntity(location, EntityType.DROPPED_ITEM);
        itemEntity.setItemStack(new ItemStack(getData().getItemRepresentation(), getData().getItemCount()));
        itemEntity.setCustomName(getData().getDisplayName());
        itemEntity.setCustomNameVisible(true);
        itemEntity.setWillAge(false);
        itemEntity.setCanPlayerPickup(false);
        itemEntity.setVelocity(new Vector());
        itemEntity.setGravity(false);
        itemEntity.setInvulnerable(true);
        itemEntity.setSilent(true);

        asName = createArmourStand(location);
        asName.addPassenger(itemEntity);

        // Check distance & time-out
        powerUpItemLocation = location;
        checkForDistTask = new BukkitRunnable() {
            @Override
            public void run() {
                MutableBoolean isPickedUp = new MutableBoolean(false);
                // Check for despawn timer
                if((System.currentTimeMillis() - spawnedTimeStamp) / 50 > getData().getDespawnDuration()) {
                    deactivate();
                    checkForDistTask.cancel();
                }

                getArena().getPlayerMap().forEach((l,r) -> {
                    var itemBox = new BoundingBox(
                            powerUpItemLocation.getX(),
                            powerUpItemLocation.getY(),
                            powerUpItemLocation.getZ(),
                            powerUpItemLocation.getX(),
                            powerUpItemLocation.getY(),
                            powerUpItemLocation.getZ()).expand(getData().getPickupRange());

                    var collide = r.getPlayer().getBoundingBox().overlaps(itemBox);
                    itemEntity.setCustomName(getData().getDisplayName());
                    var pickupDist = getData().getPickupRange();
                    if(collide && !(boolean)isPickedUp.getValue() && getState() == PowerUpState.DROPPED) {
                        if(!checkForDistTask.isCancelled()) checkForDistTask.cancel();
                        var sameType = getSamePowerUp();
                        if(sameType != null) sameType.deactivate();
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

    public PowerUp getSamePowerUp() {
        var sameType = getArena().getPowerUps().stream()
                .filter(x -> x.getState() == PowerUpState.ACTIVATED &&
                             x.getData().getName().equals(getData().getName()) &&
                             x != this)
                .findFirst();
        return sameType.orElse(null);
    }

    public void removePowerUpItem() {
        if(itemEntity != null) {
            itemEntity.remove();
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
