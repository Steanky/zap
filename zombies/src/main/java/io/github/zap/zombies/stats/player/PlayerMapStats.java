package io.github.zap.zombies.stats.player;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlayerMapStats {

    int wins = 0;

    int knockDowns = 0;

    int deaths = 0;

    int kills = 0;

    int roundsSurvived = 0;

    int bestRound = 0;

    int doorsOpened = 0;

    int windowsRepaired = 0;

    int playersRevived = 0;

    int timesPlayed = 0;

    Integer bestTime = null;

    Map<Integer, Integer> bestTimes = new HashMap<>();

}
