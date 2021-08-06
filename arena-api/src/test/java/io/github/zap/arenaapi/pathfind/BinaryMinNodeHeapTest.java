package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

public class BinaryMinNodeHeapTest {
    private final BinaryMinNodeHeap heap = new BinaryMinNodeHeap(128);

    @BeforeEach
    public void setUp() {
        for(int i = 0; i < 100; i++) {
            for(int j = 0; j < 100; j++) {
                PathNode node = new PathNode(Vectors.of(i, j, i + j));
                node.score.set(i, j);
                heap.addNode(node);
            }
        }
    }

    @Test
    public void testPeekBest() {
        PathNode node = heap.peekBest();

        Assertions.assertNotNull(node);
        Assertions.assertTrue(node.heapIndex != -1);
        Assertions.assertTrue(heap.contains(node));
        Assertions.assertEquals(0, heap.indexOf(node));
        ensureHeapInvariant();
    }

    @Test
    public void testTakeBest() {
        int size = heap.size();
        Assertions.assertFalse(heap.isEmpty());

        PathNode node = heap.peekBest();
        PathNode nodeTaken = heap.takeBest();

        Assertions.assertNotNull(nodeTaken);
        Assertions.assertEquals(node, nodeTaken);
        Assertions.assertEquals(heap.size() + 1, size);
        ensureHeapInvariant();
    }

    @Test
    public void testAddNode() {
        int size = heap.size();
        Assertions.assertFalse(heap.isEmpty());

        PathNode node = new PathNode(Vectors.of(0, 0, 0));
        node.score.set(1000, 1000);

        heap.addNode(node);
        Assertions.assertEquals(node, heap.peekBest());
        Assertions.assertEquals(size + 1, heap.size());
        ensureHeapInvariant();
    }

    @Test
    public void testUpdateNode() {
        int size = heap.size();
        Assertions.assertFalse(heap.isEmpty());

        for(int i = 0; i < size; i++) {
            PathNode node = heap.nodeAt(i);
            node.score.set(69420, 69420);
            heap.replaceNode(i, node);

            PathNode retrieved = heap.nodeAt(node.heapIndex);
            Assertions.assertEquals(node, retrieved);
        }

        Assertions.assertEquals(size, heap.size());
        ensureHeapInvariant();
    }

    @Test
    public void ensureHeapInvariant() {
        int size = heap.size();
        Assertions.assertFalse(heap.isEmpty());

        PathNode[] nodes = heap.internalArray();
        for(int i = 0; i < size; i++) {
            PathNode sample = nodes[i];
            PathNode parent = getParent(i, nodes);

            if(parent != null) {
                int result = NodeComparator.instance().compare(parent, sample);
                Assertions.assertTrue(result <= 0);
            }

            Assertions.assertEquals(sample.heapIndex, i);
        }
    }

    @Test
    public void testIteration() {
        Assertions.assertFalse(heap.isEmpty());

        List<PathNode> nodes = new ArrayList<>();
        while(heap.size() > 0) {
            nodes.add(heap.takeBest());
        }

        PathNode previous = null;
        for(PathNode node : nodes) {
            if(previous != null) {
                int comparisonResult = NodeComparator.instance().compare(previous, node);
                Assertions.assertTrue(comparisonResult <= 0);
            }

            previous = node;
        }
    }

    private PathNode getParent(int child, PathNode[] nodes) {
        int index = (child - 1) >> 1;
        if(index >= 0) {
            return nodes[index];
        }

        return null;
    }
}
