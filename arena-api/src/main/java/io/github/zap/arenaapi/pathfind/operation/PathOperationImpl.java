package io.github.zap.arenaapi.pathfind.operation;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.pathfind.agent.PathAgent;
import io.github.zap.arenaapi.pathfind.calculate.AversionCalculator;
import io.github.zap.arenaapi.pathfind.calculate.HeuristicCalculator;
import io.github.zap.arenaapi.pathfind.calculate.SuccessCondition;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import io.github.zap.arenaapi.pathfind.destination.PathDestination;
import io.github.zap.arenaapi.pathfind.path.PathNode;
import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.arenaapi.pathfind.path.PathResults;
import io.github.zap.arenaapi.pathfind.step.NodeExplorer;
import io.github.zap.vector.Vectors;
import io.github.zap.vector.graph.ArrayChunkGraph;
import io.github.zap.vector.graph.ChunkGraph;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PathOperationImpl implements PathOperation {
    private final PathAgent agent;
    private final PathDestination destination;

    private final HeuristicCalculator heuristicCalculator;
    private final AversionCalculator aversionCalculator;
    private final SuccessCondition condition;
    private final NodeExplorer nodeExplorer;
    private final ChunkBounds searchArea;

    private final ChunkGraph<PathNodeImpl> visited;
    private final NodeHeap openHeap = new BinaryMinNodeHeap(32);

    private final PathNodeImpl[] sampleBuffer = new PathNodeImpl[9];

    private State state = State.STARTED;
    private PathNodeImpl currentNode;
    private PathNodeImpl bestFound;
    private PathResult result;

    PathOperationImpl(@NotNull PathAgent agent, @NotNull PathDestination destination,
                      @NotNull HeuristicCalculator heuristicCalculator, @NotNull AversionCalculator aversionCalculator,
                      @NotNull SuccessCondition condition, @NotNull NodeExplorer nodeExplorer,
                      @NotNull ChunkBounds searchArea) {
        this.agent = agent;
        this.destination = destination;
        this.heuristicCalculator = heuristicCalculator;
        this.aversionCalculator = aversionCalculator;
        this.condition = condition;
        this.nodeExplorer = nodeExplorer;
        this.searchArea = searchArea;

        visited = new ArrayChunkGraph<>(searchArea.minX(), searchArea.minZ(), searchArea.maxX(), searchArea.maxZ());
    }

    @Override
    public boolean comparableTo(@NotNull PathOperation other) {
        return other != this && nodeExplorer.equals(other.nodeExplorer()) && agent.equals(other.agent());
    }

    @Override
    public boolean step(@NotNull PathfinderContext context) {
        if(state == State.STARTED) {
            if(currentNode != null) {
                if(!openHeap.isEmpty()) {
                    currentNode = openHeap.takeBest();
                }
                else {
                    complete(false);
                    return true;
                }
            }
            else {
                currentNode = new PathNodeImpl(Vectors.asIntFloor(agent));
                currentNode.score.set(0, heuristicCalculator.compute(context, currentNode, destination));
                bestFound = currentNode;
            }

            visited.putElement(currentNode, currentNode);
            nodeExplorer.exploreNodes(context, agent, sampleBuffer, currentNode, PathNodeImpl::new);

            for(PathNodeImpl candidateNode : sampleBuffer) {
                if(candidateNode == null) {
                    break;
                }

                if(visited.hasElementAt(candidateNode)) {
                    continue;
                }

                candidateNode.parent = currentNode;
                calculateAversion(candidateNode, context.blockProvider());

                PathNodeImpl existingNode = openHeap.nodeAt(candidateNode);
                if(existingNode == null) {
                    candidateNode.score.setH(heuristicCalculator.compute(context, candidateNode, destination));
                    openHeap.addNode(candidateNode);
                }
                else if(candidateNode.score.getG() < existingNode.score.getG()) {
                    candidateNode.score.setH(existingNode.score.getH());
                    openHeap.replaceNode(existingNode.heapIndex, candidateNode);
                }

                //comparison for best path in case of inaccessible target
                if(candidateNode.score.getH() < bestFound.score.getH()) {
                    bestFound = candidateNode;
                }

                if(condition.hasCompleted(context, candidateNode, destination)) {
                    bestFound = candidateNode;
                    complete(true);
                    return true;
                }
            }
        }
        else {
            throw new IllegalStateException("Cannot call step() for PathOperation with state " + state);
        }

        return false;
    }

    @Override
    public @NotNull PathOperation.State state() {
        return state;
    }

    @Override
    public @NotNull PathResult result() {
        if(state == State.STARTED) {
            throw new IllegalStateException("Cannot get PathResult for a PathOperation that has not completed!");
        }

        if(result == null) {
            throw new IllegalStateException("Result has not been compiled for " + this);
        }

        return result;
    }

    @Override
    public int iterations() {
        return 100;
    }

    @Override
    public @NotNull PathDestination destination() {
        return destination;
    }

    @Override
    public @NotNull ChunkGraph<? extends PathNode> visitedNodes() {
        return visited;
    }

    @Override
    public @NotNull PathAgent agent() {
        return agent;
    }

    @Override
    public @NotNull ChunkBounds searchArea() {
        return searchArea;
    }

    @Override
    public @NotNull NodeExplorer nodeExplorer() {
        return nodeExplorer;
    }

    @Override
    public @Nullable PathNode currentNode() {
        return currentNode;
    }

    @Override
    public String toString() {
        return "PathOperationImpl{agent=" + agent + ", state=" + state + ", currentNode=" + currentNode + "}";
    }

    private void calculateAversion(PathNodeImpl node, BlockCollisionProvider provider) {
        BlockCollisionView standingOn = provider.getBlock(node.x(), node.y() - 1, node.z());

        node.score.setG(node.parent.score.getG() + (standingOn != null ? (aversionCalculator.aversionForMaterial(
                standingOn.data().getMaterial())) : 0) + (aversionCalculator.aversionFactor(node) *
                Vectors.distance(node, node.parent)));
    }

    private void complete(boolean success) {
        state = success ? State.SUCCEEDED : State.FAILED;
        result = PathResults.basic(bestFound.reverse(), this, destination, state);
    }
}
