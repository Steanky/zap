package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.pathfind.traversal.ChunkGraph;
import io.github.zap.arenaapi.pathfind.traversal.ArrayChunkGraph;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class PathOperationImpl implements PathOperation {
    private final PathAgent agent;
    private final Set<? extends PathDestination> destinations;
    private State state;
    private final HeuristicCalculator heuristicCalculator;
    private final SuccessCondition condition;
    private final NodeExplorer nodeExplorer;
    private final DestinationSelector destinationSelector;
    private final ChunkCoordinateProvider searchArea;

    private final ChunkGraph<PathNode> visited = new ArrayChunkGraph<>();
    private final NodeHeap openHeap = new BinaryMinNodeHeap(128);
    private final PathNode[] sampleBuffer = new PathNode[8];

    private PathDestination bestDestination;
    private PathNode currentNode;
    private PathNode bestFound;
    private PathResult result;

    PathOperationImpl(@NotNull PathAgent agent, @NotNull Set<? extends PathDestination> destinations,
                      @NotNull HeuristicCalculator heuristicCalculator, @NotNull SuccessCondition condition,
                      @NotNull NodeExplorer nodeExplorer, @NotNull DestinationSelector destinationSelector,
                      @NotNull ChunkCoordinateProvider searchArea) {
        this.agent = agent;
        this.destinations = destinations;
        this.state = State.NOT_STARTED;
        this.heuristicCalculator = heuristicCalculator;
        this.condition = condition;
        this.nodeExplorer = nodeExplorer;
        this.destinationSelector = destinationSelector;
        this.searchArea = searchArea;
    }

    @Override
    public void init(@NotNull PathfinderContext context) {
        if(state == State.NOT_STARTED) {
            state = State.STARTED;
            nodeExplorer.init(context, agent);
        }
        else {
            throw new IllegalStateException("Cannot initialize a PathOperation with state " + state);
        }
    }

    @Override
    public boolean comparableTo(@NotNull PathOperation other) {
        return other != this && nodeExplorer.equals(other.nodeProvider()) && agent.equals(other.agent());
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
                currentNode = new PathNode(null, agent);
                bestDestination = destinationSelector.selectDestination(this, currentNode);
                currentNode.score.set(0, heuristicCalculator.compute(context, currentNode, bestDestination));
                bestFound = currentNode.copy();
            }

            PathDestination best = null;
            double bestScore = Double.POSITIVE_INFINITY;

            for(PathDestination destination : destinations) {
                if(condition.hasCompleted(context, currentNode, destination)) {
                    bestFound = currentNode;
                    bestDestination = destination;
                    complete(true);
                    return true;
                }

                if(best == null || currentNode.score.getH() < bestScore) {
                    best = destination;
                    bestScore = currentNode.score.getH();
                }
            }

            if(best == null) { //couldn't find a destination
                complete(false);
                return true;
            }

            visited.putElement(currentNode.nodeX(), currentNode.nodeY(), currentNode.nodeZ(), currentNode);
            nodeExplorer.exploreNodes(sampleBuffer, currentNode);

            for(PathNode candidateNode : sampleBuffer) {
                if(candidateNode == null) {
                    break;
                }

                if(visited.hasElement(candidateNode.nodeX(), candidateNode.nodeY(), candidateNode.nodeZ())) {
                    continue;
                }

                PathNode existingNode = openHeap.nodeAt(candidateNode.nodeX(), candidateNode.nodeY(), candidateNode.nodeZ());
                if(existingNode == null) {
                    bestDestination = destinationSelector.selectDestination(this, candidateNode);
                    candidateNode.score.setH(heuristicCalculator.compute(context, candidateNode, bestDestination));
                    openHeap.addNode(candidateNode);
                }
                else if(candidateNode.score.getG() < existingNode.score.getG()) {
                    candidateNode.score.setH(existingNode.score.getH());
                    openHeap.replaceNode(existingNode.heapIndex, candidateNode);

                    if(bestFound == existingNode) {
                        bestFound = candidateNode;
                        continue;
                    }
                }

                //comparison for best path in case of inaccessible target
                if(candidateNode.score.getH() < bestFound.score.getH()) {
                    bestFound = candidateNode;
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
        if(state == State.STARTED || state == State.NOT_STARTED) {
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
    public @NotNull Set<? extends PathDestination> getDestinations() {
        return destinations;
    }

    @Override
    public @NotNull ChunkGraph<PathNode> visitedNodes() {
        return visited;
    }

    @Override
    public @NotNull PathAgent agent() {
        return agent;
    }

    @Override
    public @NotNull ChunkCoordinateProvider searchArea() {
        return searchArea;
    }

    @Override
    public @NotNull NodeExplorer nodeProvider() {
        return nodeExplorer;
    }

    @Override
    public String toString() {
        return "PathOperationImpl{agent=" + agent + ", state=" + state + ", currentNode=" + currentNode + "}";
    }

    private void complete(boolean success) {
        state = success ? State.SUCCEEDED : State.FAILED;
        result = new PathResultImpl(bestFound.reverse(), this, visited, bestDestination, state);
    }
}