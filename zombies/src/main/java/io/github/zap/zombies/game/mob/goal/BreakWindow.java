package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.SpawnpointData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class BreakWindow extends ZombiesPathfinder {
    private static final int DISTANCE_CHECK_TICKS = 5;

    private ZombiesArena arena;
    private SpawnpointData spawnpoint;
    private WindowData window;
    private Vector destination;
    private boolean completed;

    private int counter;

    private final int breakTicks;
    private final int breakCount;
    private final double breakReachSquared;

    public BreakWindow(AbstractEntity entity, int breakTicks, int breakCount, double breakReachSquared) {
        super(entity, Zombies.ARENA_METADATA_NAME, Zombies.SPAWNPOINT_METADATA_NAME);
        this.breakTicks = breakTicks;
        this.breakCount = breakCount;
        this.breakReachSquared = breakReachSquared;
    }

    @Override
    public boolean canStart() {
        if(!completed) {
            if(arena == null && spawnpoint == null && window == null) {
                arena = getMetadata(Zombies.ARENA_METADATA_NAME);
                spawnpoint = getMetadata(Zombies.SPAWNPOINT_METADATA_NAME);
                window = arena.getMap().windowAt(spawnpoint.getWindowFace());

                destination = spawnpoint.getTarget();
                if(destination == null) { //don't start if we have no destination
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

        if(counter % DISTANCE_CHECK_TICKS == 0) {
            if(getProxy().getDistanceToSquared(getHandle(), destination.getX(), destination.getY(), destination.getZ()) < breakReachSquared) {
                Entity attackingEntity = window.getAttackingEntityProperty().getValue(arena);
                if(attackingEntity != null && getEntity().getUniqueId() == attackingEntity.getUniqueId()) {
                    window.getAttackingEntityProperty().setValue(arena, null);
                }

                completed = true;
            }
        }

        getProxy().lookAtPosition(getHandle().getControllerLook(), destination.getX(), destination.getY(), destination.getZ(), 30.0F, 30.0F);
        getProxy().navigateToLocation(getHandle(), destination.getX(), destination.getY(), destination.getZ(), 1);
    }
}