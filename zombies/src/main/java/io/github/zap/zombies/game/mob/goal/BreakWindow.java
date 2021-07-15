package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.pathfind.PathDestination;
import io.github.zap.arenaapi.pathfind.PathOperation;
import io.github.zap.arenaapi.pathfind.PathResult;
import io.github.zap.arenaapi.pathfind.PathTarget;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.WindowData;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Set;

public class BreakWindow extends BasicMetadataPathfinder {
    private ZombiesArena arena;
    private WindowData window;
    private Vector destination;
    private boolean completed;

    private int counter;
    private int navCounter;

    private final int breakTicks;
    private final int breakCount;
    private final double breakReachSquared;

    public BreakWindow(AbstractEntity entity, AttributeValue[] values, double speed, int breakTicks, int breakCount,
                       double breakReachSquared) {
        super(entity, values, speed, 0.5);
        this.breakTicks = breakTicks;
        this.breakCount = breakCount;
        this.breakReachSquared = breakReachSquared;
        navCounter = self.getRandom().nextInt(5) + 15;
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
    public void onStart() { }

    @Override
    public void onEnd() { }

    @Override
    public void doTick() {
        if(++counter == breakTicks) {
            Vector center = window.getCenter();
            if(getProxy().getDistanceToSquared(self, center.getX(), center.getY(), center.getZ()) < breakReachSquared) {
                arena.tryBreakWindow(self.getBukkitEntity(), window, breakCount);
            }

            counter = 0;
        }

        if(!(window.getFaceBounds().contains(self.locX(), self.locY(), self.locZ()) ||
                window.getInteriorBounds().contains(self.locX(), self.locY(), self.locZ()))) {
            Entity attackingEntity = window.getAttackingEntityProperty().getValue(arena);
            if(attackingEntity != null && getEntity().getUniqueId() == attackingEntity.getUniqueId()) {
                window.getAttackingEntityProperty().setValue(arena, null);
            }

            completed = true;
        }
        else {
            if(++navCounter >= 10) {
                getProxy().lookAtPosition(self.getControllerLook(), destination.getX(), destination.getY(),
                        destination.getZ(), 30.0F, 30.0F);

                getHandler().queueOperation(PathOperation.forEntityWalking(getEntity().getBukkitEntity(), Set.of(
                        PathDestination.fromVector(destination,  new PathTarget() {})),
                        3), arena.getWorld());

                navCounter = self.getRandom().nextInt(10);
            }

            PathResult result = getHandler().tryTakeResult();
            if(result != null) {
                getNavigator().navigateAlongPath(result.toPathEntity(), 1);
            }
        }
    }
}
