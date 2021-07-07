package io.github.zap.arenaapi.pathfind.traversal;

import io.github.zap.arenaapi.pathfind.*;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChunkedNodeGraphTest {
    private NodeGraph nodeGraph;
    private final Map<PathNode, PathNode> mapGraph = new HashMap<>();
    private static final int cuberootItersOver2 = 50;

    @Before
    public void setUp() {
        nodeGraph = new ChunkedNodeGraph();

        for(int i = -cuberootItersOver2; i < cuberootItersOver2; i++) {
            for(int j = -cuberootItersOver2; j < cuberootItersOver2; j++) {
                for(int k = -cuberootItersOver2; k < cuberootItersOver2; k++) {
                    nodeGraph.chainNode(i, j, k, new PathNode(i, j, k));
                    PathNode node = new PathNode(i, j, k);
                    mapGraph.put(node, node);
                }
            }
        }
    }

    @After
    public void tearDown() {
        nodeGraph = null;
    }

    @Test
    public void containsNode() {
        for(int i = -cuberootItersOver2; i < cuberootItersOver2; i++) {
            for(int j = -cuberootItersOver2; j < cuberootItersOver2; j++) {
                for(int k = -cuberootItersOver2; k < cuberootItersOver2; k++) {
                    Assert.assertTrue(nodeGraph.containsNode(i, j, k));
                }
            }
        }
    }

    @Test
    public void removeNode() {
        for(int i = -cuberootItersOver2; i < cuberootItersOver2; i++) {
            for(int j = -cuberootItersOver2; j < cuberootItersOver2; j++) {
                for(int k = -cuberootItersOver2; k < cuberootItersOver2; k++) {
                    nodeGraph.removeNode(i, j, k);
                }
            }
        }

        for(int i = -cuberootItersOver2; i < cuberootItersOver2; i++) {
            for(int j = -cuberootItersOver2; j < cuberootItersOver2; j++) {
                for(int k = -cuberootItersOver2; k < cuberootItersOver2; k++) {
                    Assert.assertFalse(nodeGraph.containsNode(i, j, k));
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
                    if(nodeGraph.containsNode(i, j, k)) {
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
                    nodeGraph.removeNode(i, j, k);
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