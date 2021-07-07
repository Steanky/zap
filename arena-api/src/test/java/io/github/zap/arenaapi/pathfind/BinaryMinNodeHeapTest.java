package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.pathfind.traversal.ChunkedNodeGraph;
import org.bukkit.util.Vector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BinaryMinNodeHeapTest {
    private BinaryMinNodeHeap heap;

    @Before
    public void setUp() {
        heap = new BinaryMinNodeHeap(128);
        for(int i = 0; i < 100; i++) {
            for(int j = 0; j < 100; j++) {
                PathNode node = new PathNode(null, new Vector(i, j, i+j));
                node.score.set(i, j);
                heap.addNode(node);
            }
        }
    }

    @After
    public void tearDown() {
        heap = null;
    }

    @Test
    public void testPeekBest() {
        Assert.assertFalse(heap.isEmpty());

        PathNode node = heap.peekBest();
        Assert.assertNotNull(node);
        Assert.assertTrue(node.heapIndex != -1);
        Assert.assertTrue(heap.contains(node));
        Assert.assertEquals(0, heap.indexOf(node));
        ensureHeapInvariant();
    }

    @Test
    public void testTakeBest() {
        int size = heap.size();
        Assert.assertFalse(heap.isEmpty());

        PathNode node = heap.peekBest();
        PathNode nodeTaken = heap.takeBest();

        Assert.assertNotNull(nodeTaken);
        Assert.assertEquals(node, nodeTaken);
        Assert.assertEquals(heap.size() + 1, size);
        ensureHeapInvariant();
    }

    @Test
    public void testAddNode() {
        int size = heap.size();
        Assert.assertFalse(heap.isEmpty());

        PathNode node = new PathNode(null, new Vector(0, 0, 0));
        node.score.set(1000, 1000);

        heap.addNode(node);
        Assert.assertEquals(node, heap.peekBest());
        Assert.assertEquals(size + 1, heap.size());
        ensureHeapInvariant();
    }

    @Test
    public void testUpdateNode() {
        int size = heap.size();
        Assert.assertFalse(heap.isEmpty());

        for(int i = 0; i < size; i++) {
            PathNode node = heap.nodeAt(i);
            node.score.set(69420, 69420);
            heap.replaceNode(i, node);

            PathNode retrieved = heap.nodeAt(node.heapIndex);
            Assert.assertEquals(node, retrieved);
        }

        Assert.assertEquals(size, heap.size());
        ensureHeapInvariant();
    }

    @Test
    public void ensureHeapInvariant() {
        int size = heap.size();
        Assert.assertFalse(heap.isEmpty());

        PathNode[] nodes = heap.internalArray();
        for(int i = 0; i < size; i++) {
            PathNode sample = nodes[i];
            PathNode parent = getParent(i, nodes);

            if(parent != null) {
                int result = NodeComparator.instance().compare(parent, sample);
                Assert.assertTrue(result <= 0);
            }

            Assert.assertEquals(sample.heapIndex, i);
        }
    }

    @Test
    public void testIteration() {
        Assert.assertFalse(heap.isEmpty());

        List<PathNode> nodes = new ArrayList<>();
        while(heap.size() > 0) {
            nodes.add(heap.takeBest());
        }

        PathNode previous = null;
        for(PathNode node : nodes) {
            if(previous != null) {
                int comparisonResult = NodeComparator.instance().compare(previous, node);
                Assert.assertTrue(comparisonResult <= 0);
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