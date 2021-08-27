package io.github.zap.arenaapi.pathfind.operation;

import io.github.zap.arenaapi.pathfind.agent.PathAgent;
import io.github.zap.arenaapi.pathfind.agent.PathAgents;
import io.github.zap.arenaapi.pathfind.calculate.*;
import io.github.zap.arenaapi.pathfind.chunk.ChunkCoordinateProvider;
import io.github.zap.arenaapi.pathfind.chunk.ChunkCoordinateProviders;
import io.github.zap.arenaapi.pathfind.destination.PathDestination;
import io.github.zap.arenaapi.pathfind.destination.PathDestinations;
import io.github.zap.arenaapi.pathfind.path.PathTarget;
import io.github.zap.arenaapi.pathfind.step.NodeExplorer;
import io.github.zap.arenaapi.pathfind.step.NodeExplorers;
import io.github.zap.arenaapi.pathfind.step.NodeStepper;
import io.github.zap.arenaapi.pathfind.step.NodeSteppers;
import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PathOperationBuilder {
    private static final int DEFAULT_PATHFIND_RADIUS = 3;
    private static final double DEFAULT_JUMP_HEIGHT = 1.125;
    private static final double DEFAULT_FALL_TOLERANCE = 16;

    private PathAgent agent;
    private Entity agentEntity;
    private double jumpHeight = DEFAULT_JUMP_HEIGHT;
    private double fallTolerance = DEFAULT_FALL_TOLERANCE;
    private PathDestination destination;
    private HeuristicCalculator heuristicCalculator;
    private AversionCalculator aversionCalculator;
    private SuccessCondition successCondition;
    private NodeExplorer nodeExplorer;
    private ChunkCoordinateProvider chunkCoordinateProvider;
    private NodeStepper nodeStepper;
    private int pathfindRadius = DEFAULT_PATHFIND_RADIUS;

    public PathOperationBuilder() {}

    public @NotNull PathOperationBuilder withAgent(@NotNull PathAgent agent) {
        this.agent = agent;
        return this;
    }

    public @NotNull PathOperationBuilder withAgent(@NotNull Entity agent) {
        this.agentEntity = agent;
        return this;
    }

    public @NotNull PathOperationBuilder withJumpHeight(double jumpHeight) {
        this.jumpHeight = jumpHeight;
        return this;
    }

    public @NotNull PathOperationBuilder withFallTolerance(double fallTolerance) {
        this.fallTolerance = fallTolerance;
        return this;
    }

    public @NotNull PathOperationBuilder withDestination(@NotNull PathDestination destination) {
        this.destination = destination;
        return this;
    }

    public @NotNull PathOperationBuilder withDestination(@NotNull Entity destination, @NotNull PathTarget target) {
        Location location = destination.getLocation();
        return withDestination(PathDestinations.basic(target, location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    public @NotNull PathOperationBuilder withDestination(@NotNull Vector3I destination) {
        return withDestination(PathDestinations.basic(new PathTarget() {}, destination));
    }

    public @NotNull PathOperationBuilder withHeuristic(@Nullable HeuristicCalculator heuristicCalculator) {
        this.heuristicCalculator = heuristicCalculator;
        return this;
    }

    public @NotNull PathOperationBuilder withExplorer(@Nullable NodeExplorer nodeExplorer) {
        this.nodeExplorer = nodeExplorer;
        return this;
    }

    public @NotNull PathOperationBuilder withRange(@Nullable ChunkCoordinateProvider chunkCoordinateProvider) {
        this.chunkCoordinateProvider = chunkCoordinateProvider;
        return this;
    }

    public @NotNull PathOperationBuilder withRange(int pathfindRadius) {
        this.pathfindRadius = pathfindRadius;
        return this;
    }

    public @NotNull PathOperationBuilder withStepper(@Nullable NodeStepper nodeStepper) {
        this.nodeStepper = nodeStepper;
        return this;
    }

    public @NotNull PathOperation build() {
        if(agent == null && agentEntity == null) {
            throw new NullPointerException("Must specify an agent!");
        }

        Objects.requireNonNull(destination, "Must specify a destination!");

        heuristicCalculator = heuristicCalculator == null ? HeuristicCalculators.distanceOnly() : heuristicCalculator;
        aversionCalculator = aversionCalculator == null ? AversionCalculators.defaultWalk() : aversionCalculator;
        successCondition = successCondition == null ? SuccessCondition.SAME_BLOCK : successCondition;
        agent = agent == null ? (PathAgents.fromVector(Vectors.of(agentEntity.getLocation()), agentEntity.getWidth(),
                agentEntity.getHeight(), jumpHeight, fallTolerance)) : agent;
        chunkCoordinateProvider = chunkCoordinateProvider == null ?
                ChunkCoordinateProviders.squareFromCenter(Vectors.asChunk(agent), pathfindRadius) : chunkCoordinateProvider;
        nodeExplorer = nodeExplorer == null ? NodeExplorers.basicWalk(nodeStepper == null ?
                NodeSteppers.basicWalk() : nodeStepper, chunkCoordinateProvider) : nodeExplorer;

        return new PathOperationImpl(agent, destination, heuristicCalculator, aversionCalculator, successCondition,
                nodeExplorer, chunkCoordinateProvider);
    }
}
