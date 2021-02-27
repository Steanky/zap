package io.github.zap.zombies.game.mob.goal;

import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.SpawnpointData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.AbstractPlayer;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ai.Pathfinder;
import io.lumine.xikage.mythicmobs.mobs.ai.PathfindingGoal;
import io.lumine.xikage.mythicmobs.util.annotations.MythicAIGoal;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Optional;

@MythicAIGoal (
        name = "breakWindow"
)
public class BreakWindow extends Pathfinder implements PathfindingGoal {
    private boolean metadataLoaded;

    private final int attackTicks;
    private final double attackReachSquared;

    private ZombiesArena arena;
    private AbstractPlayer targetPlayer;

    private int counter = 0;

    public BreakWindow(AbstractEntity entity, String line, MythicLineConfig mlc) {
        super(entity, line, mlc);
        this.goalType = GoalType.MOVE_LOOK;

        attackTicks = mlc.getInteger("attackTicks", 20);
        attackReachSquared = mlc.getDouble("attackReachSquared", 4);
    }

    private boolean loadMetadata() {
        Optional<Object> arenaOptional = entity.getMetadata(Zombies.ARENA_METADATA_NAME);
        Optional<Object> spawnpointOptional = entity.getMetadata(Zombies.SPAWNPOINT_METADATA_NAME);

        if(arenaOptional.isPresent() && spawnpointOptional.isPresent()) {
            arena = (ZombiesArena) arenaOptional.get();
            return true;
        }
        else {
            arena = null;
            return false;
        }
    }

    @Override
    public boolean shouldStart() {
        if(!metadataLoaded) {
            metadataLoaded = loadMetadata();
            return metadataLoaded;
        }

        return targetPlayer != null;
    }

    @Override
    public void start() {
        ai().navigateToLocation(entity, targetPlayer.getLocation(), 69420);
    }

    @Override
    public void tick() {

    }

    @Override
    public boolean shouldEnd() {
        return false;
    }

    @Override
    public void end() { }
}
