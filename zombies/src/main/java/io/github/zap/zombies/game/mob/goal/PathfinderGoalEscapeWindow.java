package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.SpawnpointData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitWorld;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.Pathfinder;
import io.lumine.xikage.mythicmobs.mobs.ai.PathfindingGoal;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.Vec3D;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.util.Vector;

import java.util.Optional;

@MythicAIGoal(
        author = "Steank",
        name = "escapeWindow",
        description = "Used by zombies to navigate out of windows."
)
public class PathfinderGoalEscapeWindow extends Pathfinder implements PathfindingGoal {
    private ZombiesArena arena;
    private final EntityCreature nmsEntity;

    private final double breakReachSquared;
    private final int breakIncrement;
    private final int breakTicks;

    private WindowData targetWindow = null;
    private AbstractLocation windowCenter;
    private AbstractLocation destination;

    private boolean hasWindow = false;
    private boolean loadedMetadata = false;

    private int tickCounter = 0;

    private static final int SEARCH_DISTANCE = 32;

    public PathfinderGoalEscapeWindow(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        setGoalType(GoalType.MOVE_LOOK);

        nmsEntity = (EntityCreature)((CraftEntity)entity.getBukkitEntity()).getHandle();

        breakReachSquared = mlc.getInteger("breakReachSquared", 4);
        breakIncrement = mlc.getInteger("breakIncrement", 1);
        breakTicks = mlc.getInteger("breakTicks", 20);
    }

    private void loadMetadata() {
        Optional<Object> optManager = entity.getMetadata(Zombies.ARENA_METADATA_NAME);
        Optional<Object> optSpawnpoint = entity.getMetadata(Zombies.SPAWNPOINT_METADATA_NAME);

        if(optManager.isPresent() && optSpawnpoint.isPresent()) {
            arena = (ZombiesArena) optManager.get();
            SpawnpointData spawnPoint = (SpawnpointData)optSpawnpoint.get();

            Vector target = spawnPoint.getTarget();

            if(target != null) {
                destination = new AbstractLocation(entity.getWorld(), target.getBlockX(), target.getBlockY(),
                        target.getBlockZ());

                targetWindow = arena.getMap().windowAt(spawnPoint.getWindowFace()); //get target window using lookup
            }

            loadedMetadata = true;
        }
    }

    public boolean shouldStart() {
        if(!loadedMetadata) {
            loadMetadata(); //several ticks will pass from when the mob is spawned and when metadata is applied
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        if(destination != null) {
            ai().navigateToLocation(this.entity, destination, SEARCH_DISTANCE);
        }
    }

    @Override
    public void tick() {
        if(hasWindow) {
            if(++tickCounter == breakTicks) {
                tryBreak();
                tickCounter = 0;
            }
        }

        if(destination != null) {
            nmsEntity.getControllerLook().a(new Vec3D(destination.getX(), destination.getY() + 1,
                    destination.getZ()));

            //TODO: test, this may not be necessary
            ai().navigateToLocation(this.entity, destination, SEARCH_DISTANCE);
        }
    }

    @Override
    public boolean shouldEnd() {
        AbstractLocation entityLocation = this.entity.getLocation();
        int radiusSquared = 4;
        return destination == null || (destination.distanceSquared(destination) <= (double) radiusSquared &&
                entityLocation.getBlockY() == destination.getBlockY());
    }

    @Override
    public void end() {
        targetWindow = null;
    }

    public void tryBreak() {
        if(entity.getEyeLocation().distanceSquared(windowCenter) <= breakReachSquared) {
            //TODO: break window
        }
    }
}
