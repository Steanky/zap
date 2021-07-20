package io.github.zap.vector.graph;

import io.github.zap.vector.Vector3I;
import io.github.zap.vector.Vectors;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ArrayChunkGraphTest {
    private Set<Vector3I> vectorsAdded;
    private ArrayChunkGraph<Vector3I> graph;
    int size = 30;

    @Before
    public void setUp() {
        vectorsAdded = new HashSet<>();
        graph = new ArrayChunkGraph<>(-5, -5, 5, 5);

        for(int i = -size; i < size; i++) {
            for(int j = 0; j < size * 2; j++) {
                for(int k = -size; k < size; k++) {
                    Vector3I vector = Vectors.of(i, j, k);
                    Assert.assertTrue(vectorsAdded.add(vector));

                    graph.putElement(i, j, k, vector);
                }
            }
        }
    }

    @After
    public void tearDown() {
        graph = null;
        vectorsAdded = null;
    }

    @Test
    public void testAdded() {
        for(Vector3I vector : vectorsAdded) {
            Assert.assertTrue(graph.hasElement(vector.x(), vector.y(), vector.z()));
            Assert.assertSame(graph.elementAt(vector.x(), vector.y(), vector.z()), vector);
        }
    }
}