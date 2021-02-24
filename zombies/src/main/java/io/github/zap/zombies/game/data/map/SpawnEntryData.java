package io.github.zap.zombies.game.data.map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpawnEntryData {
    String mobName;

    int mobCount;

    public SpawnEntryData() {}
}
