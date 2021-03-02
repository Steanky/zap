package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
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
public class BreakWindow extends ZombiesPathfinder {
    private static final int distanceCheckTicks = 5;

    private final int breakTicks;
    private final double breakReachSquared;
    private final int breakCount;

    private ZombiesArena arena;
    private WindowData window;
    private Vector destination;

    private int counter = 0;

    private boolean complete = false;

    public BreakWindow(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc, Zombies.ARENA_METADATA_NAME, Zombies.SPAWNPOINT_METADATA_NAME);
        goalType = GoalType.MOVE_LOOK;

        breakTicks = mlc.getInteger("breakTicks", 20);
        breakReachSquared = mlc.getDouble("breakReachSquared", 4);
        breakCount = mlc.getInteger("breakCount", 1);
    }

    @Override
    public void start() { }

    @Override
    public void tick() {
        if(++counter == breakTicks) {
            Vector center = window.getCenter();
            if(getProxy().getDistanceToSquared(getNmsEntity(), center.getX(), center.getY(), center.getZ()) < breakReachSquared) {
                arena.tryBreakWindow(getNmsEntity().getBukkitEntity(), window, breakCount);
            }

            counter = 0;
        }

        if(counter % distanceCheckTicks == 0) {
            if(getProxy().getDistanceToSquared(getNmsEntity(), destination.getX(), destination.getY(), destination.getZ()) < breakReachSquared) {
                Entity attackingEntity = window.getAttackingEntityProperty().getValue(arena);
                if(attackingEntity != null && entity.getUniqueId() == attackingEntity.getUniqueId()) {
                    window.getAttackingEntityProperty().setValue(arena, null);
                }

                complete = true;
            }
        }

        getProxy().navigateToLocation(getNmsEntity(), destination.getX(), destination.getY(), destination.getZ(), 1);
    }

    @Override
    public boolean canStart() {
        if(!complete) {
            SpawnpointData spawnpoint = getMetadata(Zombies.SPAWNPOINT_METADATA_NAME);

            arena = getMetadata(Zombies.ARENA_METADATA_NAME);
            window = arena.getMap().windowAt(spawnpoint.getWindowFace());
            destination = spawnpoint.getTarget();

            return window != null && destination != null;
        }

        return false;
    }

    @Override
    public boolean canEnd() {
        return complete || !arena.runAI();
    }

    @Override
    public void end() { }
}