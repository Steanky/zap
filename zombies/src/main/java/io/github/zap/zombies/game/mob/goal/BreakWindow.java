package io.github.zap.zombies.game.mob.goal;

import io.github.zap.arenaapi.Property;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesArenaState;
import io.github.zap.zombies.game.data.map.SpawnpointData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.Pathfinder;
import io.lumine.xikage.mythicmobs.mobs.ai.PathfindingGoal;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Optional;

@MythicAIGoal (
        name = "breakWindow"
)
public class BreakWindow extends Pathfinder implements PathfindingGoal {
    private boolean metadataLoaded;

    private final int breakTicks;
    private final double breakReachSquared;
    private final int breakCount;

    private ZombiesArena arena;
    private WindowData window;
    private AbstractLocation destination;

    private int counter = 0;

    private boolean complete = false;

    public BreakWindow(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        this.goalType = GoalType.MOVE_LOOK;

        breakTicks = mlc.getInteger("breakTicks", 20);
        breakReachSquared = mlc.getDouble("breakReachSquared", 4);
        breakCount = mlc.getInteger("breakCount", 1);
    }

    private boolean loadMetadata() {
        Optional<Object> arenaOptional = entity.getMetadata(Zombies.ARENA_METADATA_NAME);
        Optional<Object> spawnpointOptional = entity.getMetadata(Zombies.SPAWNPOINT_METADATA_NAME);

        if(arenaOptional.isPresent() && spawnpointOptional.isPresent()) {
            arena = (ZombiesArena) arenaOptional.get();
            SpawnpointData spawnpoint = (SpawnpointData) spawnpointOptional.get();

            Vector target = spawnpoint.getTarget();

            if(target != null) {
                destination = BukkitAdapter.adapt(new Location(BukkitAdapter.adapt(entity.getWorld()), target.getX() + 0.5,
                        target.getY(), target.getZ() + 0.5));

                window = arena.getMap().windowAt(spawnpoint.getWindowFace());
            }
            else {
                window = null;
                destination = null;
                complete = true;
            }

            return true;
        }
        else {
            arena = null;
            window = null;
            destination = null;
            complete = true;

            return false;
        }
    }

    @Override
    public boolean shouldStart() {
        if(!metadataLoaded) {
            metadataLoaded = loadMetadata();

            if(!metadataLoaded) {
                return false;
            }
        }

        return !complete && arena.runAI() && window != null;
    }

    @Override
    public void start() {
        ai().navigateToLocation(entity, destination, 0);
    }

    @Override
    public void tick() {
        if(++counter == breakTicks) {
            if(window.getCenter().distanceSquared(BukkitAdapter.adapt(entity.getLocation().toVector())) < breakReachSquared) {
                arena.tryBreakWindow(BukkitAdapter.adapt(entity), window, breakCount);
            }

            counter = 0;
        }

        if(entity.getLocation().distanceSquared(destination) < 2) {
            complete = true;

            Entity attackingEntity = window.getAttackingEntityProperty().getValue(arena);

            if(attackingEntity != null && entity.getUniqueId() == attackingEntity.getUniqueId()) {
                window.getAttackingEntityProperty().setValue(arena, null);
            }
        }

        ai().navigateToLocation(entity, destination, 0);
    }

    @Override
    public boolean shouldEnd() {
        return complete || !arena.runAI() || window == null;
    }

    @Override
    public void end() { }
}