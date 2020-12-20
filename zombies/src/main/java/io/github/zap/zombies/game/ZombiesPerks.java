package io.github.zap.zombies.game;

import io.github.zap.arenaapi.event.RepeatingEvent;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.MapData;
import lombok.Getter;

/**
 * Container class; each instance holds all perks for a specific player.
 */
@Getter
public class ZombiesPerks {
    private final SpeedPerk speedPerk;
    private final QuickFire quickFire;

    public ZombiesPerks(ZombiesPlayer player) {
        MapData map = player.getArena().getMap();

        speedPerk = new SpeedPerk(player, new RepeatingEvent(Zombies.getInstance(), 0,
                map.getSpeedPerkReapplyInterval()), map.getSpeedPerkMaxLevel(), map.getSpeedPerkDuration(),
                map.getSpeedPerkAmplifier());

        quickFire = new QuickFire(player, 0, map.getQuickFireDelayReduction());
    }
}
