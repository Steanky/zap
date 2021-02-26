package io.github.zap.zombies.game.data.map;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SpawnRule {
    String name;
    boolean blacklist;
    Set<String> mobSet;
}
