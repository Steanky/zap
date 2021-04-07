package io.github.zap.zombies.stats.game;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MapStats {

    int wins;

    int knockDowns;

    int deaths;

    int kills;

    int roundsSurvived;

    int bestRound;

    int doorsOpened;

    int windowsRepaired;

    int playersRevived;

    Map<Integer, Integer> bestTimes = new HashMap<>();

}
