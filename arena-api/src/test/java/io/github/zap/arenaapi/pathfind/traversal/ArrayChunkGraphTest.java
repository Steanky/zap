package io.github.zap.arenaapi.pathfind.traversal;

import io.github.zap.arenaapi.pathfind.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ArrayChunkGraphTest {
    private ChunkGraph<PathNode> chunkGraph;
    private final Map<PathNode, PathNode> mapGraph = new HashMap<>();
    private static final int cuberootItersOver2 = 50;

    @Before
    public void setUp() {
        chunkGraph = new ArrayChunkGraph<>();

        for(int i = -cuberootItersOver2; i < cuberootItersOver2; i++) {
            for(int j = -cuberootItersOver2; j < cuberootItersOver2; j++) {
                for(int k = -cuberootItersOver2; k < cuberootItersOver2; k++) {
                    chunkGraph.putElement(i, j, k, new PathNode(i, j, k));
                    PathNode node = new PathNode(i, j, k);
                    mapGraph.put(node, node);
                }
            }
        }
    }

    @After
    public void tearDown() {
        chunkGraph = null;
    }

    @Test
    public void containsNode() {
        for(int i = -cuberootItersOver2; i < cuberootItersOver2; i++) {
            for(int j = -cuberootItersOver2; j < cuberootItersOver2; j++) {
                for(int k = -cuberootItersOver2; k < cuberootItersOver2; k++) {
                    Assert.assertTrue(chunkGraph.hasElement(i, j, k));
                }
            }
        }
    }

    @Test
    public void removeNode() {
        for(int i = -cuberootItersOver2; i < cuberootItersOver2; i++) {
            for(int j = -cuberootItersOver2; j < cuberootItersOver2; j++) {
                for(int k = -cuberootItersOver2; k < cuberootItersOver2; k++) {
                    chunkGraph.removeElement(i, j, k);
                }
            }
        }

        for(int i = -cuberootItersOver2; i < cuberootItersOver2; i++) {
            for(int j = -cuberootItersOver2; j < cuberootItersOver2; j++) {
                for(int k = -cuberootItersOver2; k < cuberootItersOver2; k++) {
                    Assert.assertFalse(chunkGraph.hasElement(i, j, k));
                }
            }
        }
    }

    @Test
    public void benchmark() {
        int zzz = 0;
        long timeBeforeGraphContains = System.currentTimeMillis();
        for(int i = -cuberootItersOver2; i < cuberootItersOver2; i++) {
            for(int j = -cuberootItersOver2; j < cuberootItersOver2; j++) {
                for(int k = -cuberootItersOver2; k < cuberootItersOver2; k++) {
                    if(chunkGraph.hasElement(i, j, k)) {
                        zzz++;
                    }
                }
            }
        }
        long timeAfterGraphContains = System.currentTimeMillis();
        long diffGraphContains = timeAfterGraphContains - timeBeforeGraphContains;
        System.out.println("Time elapsed running contains on nodeGraph with " + Math.pow(cuberootItersOver2, 3) + " nodes: " + diffGraphContains);

        long timeBeforeMapContains = System.currentTimeMillis();
        for(int i = -cuberootItersOver2; i < cuberootItersOver2; i++) {
            for(int j = -cuberootItersOver2; j < cuberootItersOver2; j++) {
                for(int k = -cuberootItersOver2; k < cuberootItersOver2; k++) {
                    if(mapGraph.containsKey(new PathNode(i, j, k))) {
                        zzz++;
                    }
                }
            }
        }
        long timeAfterMapContains = System.currentTimeMillis();
        long diffMapContains = timeAfterMapContains - timeBeforeMapContains;
        System.out.println("Time elapsed running contains on map with " + Math.pow(cuberootItersOver2, 3) + " nodes: " + diffMapContains);

        long timeBeforeGraphRemove = System.currentTimeMillis();
        for(int i = -cuberootItersOver2; i < cuberootItersOver2; i++) {
            for(int j = -cuberootItersOver2; j < cuberootItersOver2; j++) {
                for(int k = -cuberootItersOver2; k < cuberootItersOver2; k++) {
                    chunkGraph.removeElement(i, j, k);
                }
            }
        }
        long timeAfterGraphRemove = System.currentTimeMillis();
        long diffGraphRemove = timeAfterGraphRemove - timeBeforeGraphRemove;
        System.out.println("Time elapsed running remove on nodeGraph with " + Math.pow(cuberootItersOver2, 3) + " nodes: " + diffGraphRemove);

        long timeBeforeMapRemove = System.currentTimeMillis();
        for(int i = -cuberootItersOver2; i < cuberootItersOver2; i++) {
            for(int j = -cuberootItersOver2; j < cuberootItersOver2; j++) {
                for(int k = -cuberootItersOver2; k < cuberootItersOver2; k++) {
                    mapGraph.remove(new PathNode(i, j, k));
                }
            }
        }
        long timeAfterMapRemove = System.currentTimeMillis();
        long diffMapRemove = timeAfterMapRemove - timeBeforeMapRemove;
        System.out.println("Time elapsed running remove on map with " + Math.pow(cuberootItersOver2, 3) + " nodes: " + diffMapRemove);
    }
}