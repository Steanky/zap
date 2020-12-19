package io.github.zap.zombies.game.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TestData {
    @Getter
    private String string;

    private TestData() {}
}
