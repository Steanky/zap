package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vectors;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathNodeTest {
    private final List<PathNode> testNodes = new ArrayList<>();

    @Before
    public void setUp() {
        PathNode node = null;
        for(int i = 0; i < 50; i++) {
            for(int j = 0; j < 50; j++) {
                for(int k = 0; k < 50; k++) {
                    if(node != null) {
                        testNodes.add((node = node.chain(i, j, k)));
                    }
                    else {
                        PathNode first = new PathNode(Vectors.of(i, j, k));
                        node = first;
                        testNodes.add(first);
                    }
                }
            }
        }
    }

    @Test
    public void reverse() {
        PathNode reversed = testNodes.get(testNodes.size() - 1).reverse();

        Assert.assertTrue(deepEquals(reversed, testNodes.get(0)));
        Assert.assertTrue(ensureNonLoopingParentTraversal(reversed));
        Assert.assertTrue(ensureNonLoopingChildTraversal(testNodes.get(testNodes.size() - 1)));

        int parentCount = 0;
        while(reversed.parent != null) {
            reversed = reversed.parent;
            parentCount++;
        }

        Assert.assertEquals(testNodes.size(), parentCount + 1);
    }

    private boolean deepEquals(PathNode one, PathNode two) {
        while(one != null) {
            PathNode oneChild = one.child;
            PathNode twoChild = two == null ? null : two.child;

            if(oneChild != null && twoChild != null) {
                if(!oneChild.equals(twoChild))  {
                    return false;
                }
            }
            else return oneChild == twoChild;

            if(!one.equals(two)) {
                return false;
            }

            one = one.parent;
            two = two.parent;
        }

        return two == null;
    }

    private boolean ensureNonLoopingParentTraversal(PathNode node) {
        Set<PathNode> previousNodes = new HashSet<>();
        previousNodes.add(node);

        while(node.parent != null) {
            node = node.parent;
            if(previousNodes.contains(node)) {
                return false;
            }

            previousNodes.add(node);
        }

        return true;
    }

    private boolean ensureNonLoopingChildTraversal(PathNode node) {
        Set<PathNode> previousNodes = new HashSet<>();
        previousNodes.add(node);

        while(node.child != null) {
            node = node.child;
            if(previousNodes.contains(node)) {
                return false;
            }

            previousNodes.add(node);
        }

        return true;
    }
}