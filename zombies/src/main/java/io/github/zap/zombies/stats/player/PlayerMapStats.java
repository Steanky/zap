package io.github.zap.zombies.stats.player;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlayerMapStats {

    int wins;

    int knockDowns;

    int deaths;

    int kills;

    int roundsSurvived;

    int bestRound;

    int doorsOpened;

    int windowsRepaired;

    int playersRevived;

    int timesPlayed;

    Map<Integer, Integer> bestTimes = new HashMap<>();

}
