package io.github.zap.arenaapi.pathfind;

import io.github.zap.arenaapi.ArenaApi;

import java.util.Random;

public class Test {
    public static void test() {
        Random random = new Random();
        NodeQueue queue = new NodeQueue(16);

        for(int i = 0; i < 10; i++) {
            PathNode node = new PathNode(i + random.nextInt(2 * (i + 1)), i + random.nextInt(2 * (i + 1)), i + random.nextInt(2 * (i + 1)));
            node.score = new Score(i + random.nextInt(2 * (i + 1)), i + random.nextInt(2 * (i + 1)));
            queue.add(node);
        }

        while(queue.size() > 0) {
            ArenaApi.info(queue.poll().toString());
        }

        ArenaApi.info("t");
    }
}
