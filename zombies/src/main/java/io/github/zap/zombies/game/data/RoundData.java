package io.github.zap.zombies.game.data;

import io.github.zap.arenaapi.serialize.DataSerializable;
import io.github.zap.arenaapi.serialize.TypeAlias;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@TypeAlias("ZombiesRound")
public class RoundData extends DataSerializable {
    /**
     * Message displayed when the round starts. overrides the normal "Round #" message
     */
    String customMessage;

    List<WaveData> waves;

    private RoundData() {}
}
