package io.github.zap.zombies.data.levels;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A level that contains a single cost field
 */
@RequiredArgsConstructor
public class CostLevel {

    @Getter
    private final int cost;

}
