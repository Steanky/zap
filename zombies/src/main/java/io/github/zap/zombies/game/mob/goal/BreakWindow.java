package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.WindowData;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class BreakWindow extends ZombiesPathfinder {
    private ZombiesArena arena;
    private WindowData window;
    private Vector destination;
    private boolean completed;

    private int counter;

    private final int breakTicks;
    private final int breakCount;
    private final double breakReachSquared;

    public BreakWindow(AbstractEntity entity, int breakTicks, int breakCount, double breakReachSquared) {
        super(entity, Zombies.ARENA_METADATA_NAME, Zombies.WINDOW_METADATA_NAME);
        this.breakTicks = breakTicks;
        this.breakCount = breakCount;
        this.breakReachSquared = breakReachSquared;
    }

    @Override
    public boolean canStart() {
        if(!completed) {
            if(arena == null && destination == null && window == null) {
                arena = getMetadata(Zombies.ARENA_METADATA_NAME);
                window = getMetadata(Zombies.WINDOW_METADATA_NAME);

                if(window != null) {
                    destination = window.getTarget();

                    if(destination == null) {
                        Zombies.warning("Entity " + getEntity().getUniqueId() + " spawned in a window that does not" +
                                " supply a target destination!");
                        completed = true;
                        return false;
                    }
                }
                else {
                    completed = true;
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean stayActive() {
        return !completed && arena.runAI();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onEnd() {

    }

    @Override
    public void doTick() {
        if(++counter == breakTicks) {
            Vector center = window.getCenter();
            if(getProxy().getDistanceToSquared(getHandle(), center.getX(), center.getY(), center.getZ()) < breakReachSquared) {
                arena.tryBreakWindow(getHandle().getBukkitEntity(), window, breakCount);
            }

            counter = 0;
        }

        if(!(window.getFaceBounds().contains(getHandle().locX(), getHandle().locY(), getHandle().locZ()) ||
                window.getInteriorBounds().contains(getHandle().locX(), getHandle().locY(), getHandle().locZ()))) {
            Entity attackingEntity = window.getAttackingEntityProperty().getValue(arena);
            if(attackingEntity != null && getEntity().getUniqueId() == attackingEntity.getUniqueId()) {
                window.getAttackingEntityProperty().setValue(arena, null);
            }

            completed = true;
        }
        else {
            getProxy().lookAtPosition(getHandle().getControllerLook(), destination.getX(), destination.getY(), destination.getZ(), 30.0F, 30.0F);
            getProxy().navigateToLocation(getHandle(), destination.getX(), destination.getY(), destination.getZ(), 1);
        }
    }
}