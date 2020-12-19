package io.github.zap.zombies.game.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoundData {
    /**
     * Message displayed when the round starts. overrides the normal "Round #" message
     */
    String customMessage;

    List<WaveData> waves;

    private RoundData() {}
}
