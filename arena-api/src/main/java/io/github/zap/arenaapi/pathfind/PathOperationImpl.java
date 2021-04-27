package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.*;

class PathOperationImpl implements PathOperation {
    private final PathAgent agent;
    private final Set<PathDestination> destinations;
    private State state;
    private final ScoreCalculator scoreCalculator;
    private final SuccessCondition condition;
    private final NodeProvider nodeProvider;
    private final DestinationSelector destinationSelector;
    private final ChunkCoordinateProvider searchArea;

    //TODO: maintain separate NodeQueue for lower-priority nodes and only take from that queue when needed
    private final NodeQueue openSet = new BinaryHeapNodeQueue(128);
    private final Map<PathNode, PathNode> visited = new HashMap<>();
    private PathDestination bestDestination;
    private PathNode currentNode;
    private PathNode bestFound;
    private PathResult result;

    PathOperationImpl(@NotNull PathAgent agent, @NotNull Set<PathDestination> destinations,
                      @NotNull ScoreCalculator scoreCalculator, @NotNull SuccessCondition condition,
                      @NotNull NodeProvider nodeProvider, @NotNull DestinationSelector destinationSelector,
                      @NotNull ChunkCoordinateProvider searchArea) {
        this.agent = agent;
        this.destinations = destinations;
        this.state = State.NOT_STARTED;
        this.scoreCalculator = scoreCalculator;
        this.condition = condition;
        this.nodeProvider = nodeProvider;
        this.destinationSelector = destinationSelector;
        this.searchArea = searchArea;
    }

    @Override
    public void init() {
        if(state == State.NOT_STARTED) {
            state = State.STARTED;
        }
        else {
            throw new IllegalStateException("Cannot initialize a PathOperation with state " + state);
        }
    }

    @Override
    public boolean allowMerge(@NotNull PathOperation other) {
        return other != this && nodeProvider.equals(other.nodeProvider()) && agent.equals(other.agent());
    }

    @Override
    public boolean step(@NotNull PathfinderContext context) {
        if(state == State.STARTED) {
            if(currentNode != null) {
                PathDestination best = null;
                double bestScore = Double.POSITIVE_INFINITY;

                for(PathDestination destination : destinations) {
                    if(condition.hasCompleted(context, currentNode, destination)) {
                        bestDestination = destination;
                        complete(true);
                        return true;
                    }

                    double score = scoreCalculator.computeH(context, currentNode, destination);
                    if(best == null || score < bestScore) {
                        best = destination;
                        bestScore = score;
                    }
                }

                if(best == null) {
                    complete(false);
                    return true;
                }

                if(openSet.size() > 0) {
                    currentNode = openSet.takeBest();
                }
                else {
                    complete(false);
                    return true;
                }
            }
            else {
                currentNode = new PathNode(null, agent);
                bestDestination = destinationSelector.selectDestination(this, currentNode);
                currentNode.score.set(0, scoreCalculator.computeH(context, currentNode, bestDestination));
                bestFound = currentNode.copy();
            }

            visited.put(currentNode, currentNode);

            PathNode[] possibleNodes = nodeProvider.generateNodes(context, currentNode);
            for(PathNode sample : possibleNodes) {
                if(sample == null) {
                    break;
                }

                if(visited.containsKey(sample)) {
                    continue;
                }

                //TODO: implement fancy optimizations
                if(nodeProvider.mayTraverse(context, agent, currentNode, sample)) {
                    bestDestination = destinationSelector.selectDestination(this, sample);

                    double g = scoreCalculator.computeG(context, currentNode, sample, bestDestination);
                    if(g < sample.score.getG()) {
                        PathNode newSample = sample.copy();
                        newSample.score.set(g, scoreCalculator.computeH(context, sample, bestDestination));
                        openSet.replaceNode(sample, newSample);
                        sample = newSample;
                    }

                    //comparison for best path in case of inaccessible target
                    if(sample.score.getF() < bestFound.score.getF()) {
                        bestFound = sample.copy();
                    }
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
    public @NotNull Set<PathDestination> getDestinations() {
        return destinations;
    }

    @Override
    public @NotNull Map<PathNode, PathNode> visitedNodes() {
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
    public @NotNull NodeProvider nodeProvider() {
        return nodeProvider;
    }

    @Override
    public String toString() {
        return "PathOperationImpl{agent=" + agent + ", state=" + state + ", currentNode=" + currentNode + "}";
    }

    private void complete(boolean success) {
        state = success ? State.SUCCEEDED : State.FAILED;
        result = new PathResultImpl(success ? currentNode.reverse() : bestFound.reverse(), this, visited,
                bestDestination, state);
    }
}