package io.github.zap.arenaapi.pathfind;

import io.github.zap.vector.Vectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

public class PathNodeTest {
    private final List<PathNode> testNodes = new ArrayList<>();

    @BeforeEach
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

        Assertions.assertTrue(deepEquals(reversed, testNodes.get(0)));
        Assertions.assertTrue(ensureNonLoopingParentTraversal(reversed));
        Assertions.assertTrue(ensureNonLoopingChildTraversal(testNodes.get(testNodes.size() - 1)));

        int parentCount = 0;
        while(reversed.parent != null) {
            reversed = reversed.parent;
            parentCount++;
        }

        Assertions.assertEquals(testNodes.size(), parentCount + 1);
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
