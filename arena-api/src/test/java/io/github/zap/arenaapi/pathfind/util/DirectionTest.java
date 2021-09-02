package io.github.zap.arenaapi.pathfind.util;

import io.github.zap.vector.Direction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DirectionTest {
    @Test
    void testOpposite() {
        for(Direction direction : Direction.values()) {
            Assertions.assertSame(direction, direction.opposite().opposite());
        }
    }
}