package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PathOperationBuilder {
    private static final int DEFAULT_PATHFIND_RADIUS = 3;

    private PathAgent agent;
    private PathDestination destination;
    private HeuristicCalculator heuristicCalculator;
    private AversionCalculator aversionCalculator;
    private SuccessCondition successCondition;
    private NodeExplorer nodeExplorer;
    private ChunkCoordinateProvider chunkCoordinateProvider;
    private NodeStepper nodeStepper;

    public PathOperationBuilder() {}

    public @NotNull PathOperationBuilder withAgent(@NotNull PathAgent agent) {
        this.agent = agent;
        return this;
    }

    public @NotNull PathOperationBuilder withAgent(@NotNull Entity agent) {
        Location location = agent.getLocation();
        return withAgent(new PathAgentImpl(new AgentCharacteristics(agent), location.getX(), location.getY(), location.getZ()));
    }

    public @NotNull PathOperationBuilder withDestination(@NotNull PathDestination destination) {
        this.destination = destination;
        return this;
    }

    public @NotNull PathOperationBuilder withDestination(@NotNull Entity destination, @NotNull PathTarget target) {
        Location location = destination.getLocation();
        return withDestination(new PathDestinationImpl(target, location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    public @NotNull PathOperationBuilder withDestination(@NotNull Vector3I destination) {
        return withDestination(new PathDestinationImpl(new PathTarget() {}, destination.x(), destination.y(), destination.z()));
    }

    public @NotNull PathOperationBuilder withHeuristic(@Nullable HeuristicCalculator heuristicCalculator) {
        this.heuristicCalculator = heuristicCalculator;
        return this;
    }

    public @NotNull PathOperationBuilder withExplorer(@Nullable NodeExplorer nodeExplorer) {
        this.nodeExplorer = nodeExplorer;
        return this;
    }

    public @NotNull PathOperationBuilder withChunks(@Nullable ChunkCoordinateProvider chunkCoordinateProvider) {
        this.chunkCoordinateProvider = chunkCoordinateProvider;
        return this;
    }

    public @NotNull PathOperationBuilder withStepper(@Nullable NodeStepper nodeStepper) {
        this.nodeStepper = nodeStepper;
        return this;
    }

    public PathOperation build() {
        Objects.requireNonNull(agent, "Must specify a PathAgent!");
        Objects.requireNonNull(destination, "Must specify a destination!");

        heuristicCalculator = heuristicCalculator == null ? HeuristicCalculator.DISTANCE_ONLY : heuristicCalculator;
        aversionCalculator = aversionCalculator == null ? AversionCalculator.DEFAULT_WALK : aversionCalculator;
        successCondition = successCondition == null ? SuccessCondition.SAME_BLOCK : successCondition;
        chunkCoordinateProvider = chunkCoordinateProvider == null ?
                ChunkCoordinateProvider.squareFromCenter(Vectors.asChunk(agent), DEFAULT_PATHFIND_RADIUS) : chunkCoordinateProvider;
        nodeExplorer = nodeExplorer == null ? new DefaultWalkNodeExplorer(agent, nodeStepper == null ?
                new WalkNodeStepper(agent.characteristics()) : nodeStepper) : nodeExplorer;

        return new PathOperationImpl(agent, destination, heuristicCalculator, aversionCalculator, successCondition,
                nodeExplorer, chunkCoordinateProvider);
    }
}
