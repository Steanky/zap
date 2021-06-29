package io.github.zap.arenaapi.pathfind;

import org.bukkit.util.Vector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BinaryMinNodeHeapTest {
    private BinaryMinNodeHeap heap;

    @Before
    public void setUp() {
        heap = new BinaryMinNodeHeap();
        for(int i = 0; i < 100; i++) {
            for(int j = 0; j < 100; j++) {
                PathNode node = new PathNode(null, new Vector(0, 0, 0));
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
        PathNode node = heap.peekBest();
        Assert.assertNotNull(node);
        Assert.assertTrue(node.heapIndex != -1);
        Assert.assertTrue(heap.contains(node));
        Assert.assertEquals(0, heap.indexOf(node));
    }

    @Test
    public void testTakeBest() {
        int size = heap.size();
        PathNode node = heap.peekBest();
        PathNode nodeTaken = heap.takeBest();

        Assert.assertNotNull(nodeTaken);
        Assert.assertEquals(node, nodeTaken);
        Assert.assertEquals(heap.size() - 1, size);
    }

    @Test
    public void testAddNode() {
        int size = heap.size();
        PathNode node = new PathNode(null, new Vector(0, 0, 0));
        node.score.set(1000, 1000);

        heap.addNode(node);
        Assert.assertEquals(node, heap.peekBest());
        Assert.assertEquals(size + 1, heap.size());
    }

    @Test
    public void testUpdateNode() {
        int size = heap.size();
        for(int i = 0; i < size; i++) {
            PathNode node = heap.nodeAt(i);
            node.score.set(69420, 69420);
            heap.updateNode(i);

            PathNode retrieved = heap.nodeAt(node.heapIndex);
            Assert.assertEquals(node, retrieved);
        }

        Assert.assertEquals(size, heap.size());
    }
}