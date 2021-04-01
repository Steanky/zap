package io.github.zap.arenaapi.pathfind;

import org.apache.commons.lang3.Validate;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public interface PathOperation {
    enum State {
        INCOMPLETE,
        SUCCEEDED,
        FAILED
    }

    boolean step(@NotNull PathfinderContext context);

    @NotNull PathOperation.State getState();

    @NotNull PathResult getResult();

    int desiredIterations();

    boolean shouldRemove();

    @NotNull Set<? extends PathDestination> getDestinations();

    @NotNull Set<PathNode> visitedNodes();

    @NotNull PathAgent getAgent();

    @NotNull ChunkRange searchArea();

    static PathOperation forAgent(@NotNull PathAgent agent, @NotNull Set<? extends PathDestination> destinations,
                                  @NotNull ScoreCalculator calculator, @NotNull SuccessCondition successCondition,
                                  @NotNull NodeProvider provider, @NotNull DestinationSelector destinationSelector,
                                  int chunkRadius) {
        Objects.requireNonNull(agent, "agent cannot be null!");
        Objects.requireNonNull(destinations, "destinations cannot be null!");
        Validate.isTrue(!destinations.isEmpty(), "destinations cannot be empty!");
        Objects.requireNonNull(calculator, "calculator cannot be null!");
        Objects.requireNonNull(successCondition,"terminationCondition cannot be null!");
        Objects.requireNonNull(provider, "provider cannot be null!");
        Objects.requireNonNull(destinationSelector, "destinationSelector cannot be null!");
        Validate.isTrue(chunkRadius >= 0, "chunkRadius cannot be negative!");

        Vector agentPos = agent.position();
        return new PathOperationImpl(agent, destinations, calculator, successCondition, provider,
                destinationSelector, new ChunkRange(agentPos.getBlockX(), agentPos.getBlockZ(), chunkRadius));
    }
}