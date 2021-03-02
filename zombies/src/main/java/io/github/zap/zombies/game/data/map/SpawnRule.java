package io.github.zap.zombies.game.data.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public class SpawnRule {
    private String name = "default";
    private boolean blacklist = true;
    private Set<String> mobSet = new HashSet<>();

    private SpawnRule() {}
}
