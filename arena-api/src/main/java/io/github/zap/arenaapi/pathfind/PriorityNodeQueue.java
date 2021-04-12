package io.github.zap.arenaapi.pathfind;

import org.jetbrains.annotations.NotNull;

import java.util.PriorityQueue;

public class PriorityNodeQueue implements NodeQueue {
    private final PriorityQueue<PathNode> nodes;

    PriorityNodeQueue(int initialCapacity) {
        nodes = new PriorityQueue<>(initialCapacity, NodeComparator.instance());
    }

    @Override
    public @NotNull PathNode peekBest() {
        PathNode best = nodes.peek();
        if(best == null) {
            throw new IndexOutOfBoundsException();
        }

        return best;
    }

    @Override
    public @NotNull PathNode takeBest() {
        PathNode best = nodes.poll();
        if(best == null) {
            throw new IndexOutOfBoundsException();
        }

        return best;
    }

    @Override
    public void addNode(@NotNull PathNode node) {
        nodes.add(node);
    }

    @Override
    public void replaceNode(@NotNull PathNode current, @NotNull PathNode newNode) {
        nodes.remove(current);
        nodes.add(newNode);
    }

    @Override
    public boolean contains(@NotNull PathNode node) {
        return nodes.contains(node);
    }

    @Override
    public int size() {
        return nodes.size();
    }
}
