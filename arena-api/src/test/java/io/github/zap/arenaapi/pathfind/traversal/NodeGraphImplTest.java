package io.github.zap.arenaapi.pathfind.traversal;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.pathfind.PathNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeGraphImplTest {
    private NodeGraph nodeGraph;
    private static int itersCuberoot = 10;

    @Before
    public void setUp() throws Exception {
        nodeGraph = new NodeGraphImpl();

        for(int i = 0; i < itersCuberoot; i++) {
            for(int j = 0; j < itersCuberoot; j++) {
                for(int k = 0; k < itersCuberoot; k++) {
                    nodeGraph.putNode(new PathNode(i, j, k), null);
                }
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        nodeGraph = null;
    }

    @Test
    public void nodeAt() {
        for(int i = 0; i < itersCuberoot; i++) {
            for(int j = 0; j < itersCuberoot; j++) {
                for(int k = 0; k < itersCuberoot; k++) {
                    System.out.println("Node at x=" + i + ", y=" + j + ", z=" + k + ": " + nodeGraph.containsNode(i, j, k));
                }
            }
        }
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
}