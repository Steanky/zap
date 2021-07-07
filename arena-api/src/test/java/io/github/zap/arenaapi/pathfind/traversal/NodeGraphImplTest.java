package io.github.zap.arenaapi.pathfind.traversal;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.pathfind.PathNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class NodeGraphImplTest {
    private NodeGraph nodeGraph;
    private Map<PathNode, PathNode> mapGraph = new HashMap<>();
    private static int itersCuberoot = 100;

    @Before
    public void setUp() throws Exception {
        nodeGraph = new NodeGraphImpl();
    }

    @After
    public void tearDown() throws Exception {
        nodeGraph = null;
    }

    @Test
    public void nodeAt() {

    }

    @Test
    public void putNode() {
    }

    @Test
    public void removeNode() {
    }

    @Test
    public void removeChunk() {
    }

    @Test
    public void containsNode() {
    }

    @Test
    public void benchmark() {

        long timeBeforeGraph = System.currentTimeMillis();
        for(int i = 0; i < itersCuberoot; i++) {
            for(int j = 0; j < itersCuberoot; j++) {
                for(int k = 0; k < itersCuberoot; k++) {
                    nodeGraph.putNode(new PathNode(i, j, k), null);
                }
            }
        }
        long timeAfterGraph = System.currentTimeMillis();
        long diffGraph = timeAfterGraph - timeBeforeGraph;
        System.out.println("Time elapsed populating nodeGraph with " + Math.pow(itersCuberoot, 3) + " nodes: " + diffGraph);

        long timeBeforeMap = System.currentTimeMillis();
        for(int i = 0; i < itersCuberoot; i++) {
            for(int j = 0; j < itersCuberoot; j++) {
                for(int k = 0; k < itersCuberoot; k++) {
                    PathNode node = new PathNode(i, j, k);
                    mapGraph.put(node, node);
                }
            }
        }
        long timeAfterMap = System.currentTimeMillis();
        long diffMap = timeAfterMap - timeBeforeMap;
        System.out.println("Time elapsed populating map with " + Math.pow(itersCuberoot, 3) + " nodes: " + diffMap);

        /////

        int zzz = 0;
        long timeBeforeGraphContains = System.currentTimeMillis();
        for(int i = 0; i < itersCuberoot; i++) {
            for(int j = 0; j < itersCuberoot; j++) {
                for(int k = 0; k < itersCuberoot; k++) {
                    if(nodeGraph.containsNode(i, j, k)) {
                        zzz++;
                    }
                }
            }
        }
        long timeAfterGraphContains = System.currentTimeMillis();
        long diffGraphContains = timeAfterGraphContains - timeBeforeGraphContains;
        System.out.println("Time elapsed running contains on nodeGraph with " + Math.pow(itersCuberoot, 3) + " nodes: " + diffGraphContains);

        long timeBeforeMapContains = System.currentTimeMillis();
        for(int i = 0; i < itersCuberoot; i++) {
            for(int j = 0; j < itersCuberoot; j++) {
                for(int k = 0; k < itersCuberoot; k++) {
                    if(mapGraph.containsKey(new PathNode(i, j, k))) {
                        zzz++;
                    }
                }
            }
        }
        long timeAfterMapContains = System.currentTimeMillis();
        long diffMapContains = timeAfterMapContains - timeBeforeMapContains;
        System.out.println("Time elapsed running contains on map with " + Math.pow(itersCuberoot, 3) + " nodes: " + diffMapContains);


        /////


        long timeBeforeGraphRemove = System.currentTimeMillis();
        for(int i = 0; i < itersCuberoot; i++) {
            for(int j = 0; j < itersCuberoot; j++) {
                for(int k = 0; k < itersCuberoot; k++) {
                    nodeGraph.removeNode(i, j, k);
                }
            }
        }
        long timeAfterGraphRemove = System.currentTimeMillis();
        long diffGraphRemove = timeAfterGraphRemove - timeBeforeGraphRemove;
        System.out.println("Time elapsed running remove on nodeGraph with " + Math.pow(itersCuberoot, 3) + " nodes: " + diffGraphRemove);

        long timeBeforeMapRemove = System.currentTimeMillis();
        for(int i = 0; i < itersCuberoot; i++) {
            for(int j = 0; j < itersCuberoot; j++) {
                for(int k = 0; k < itersCuberoot; k++) {
                    mapGraph.remove(new PathNode(i, j, k));
                }
            }
        }
        long timeAfterMapRemove = System.currentTimeMillis();
        long diffMapRemove = timeAfterMapRemove - timeBeforeMapRemove;
        System.out.println("Time elapsed running contains on map with " + Math.pow(itersCuberoot, 3) + " nodes: " + diffMapRemove);
    }
}