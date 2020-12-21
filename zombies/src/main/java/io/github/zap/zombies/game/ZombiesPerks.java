package io.github.zap.zombies.game;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.event.RepeatingEvent;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.MapData;
import io.github.zap.zombies.game.perk.*;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Container class; each instance holds all perks for a specific player.
 */
@Getter
public class ZombiesPerks implements Disposable {
    private final Map<PerkType, Perk<?>> perks = new HashMap<>();

    public ZombiesPerks(ZombiesPlayer player) {
        MapData map = player.getArena().getMap();

        perks.put(PerkType.SPEED, new SpeedPerk(player, new RepeatingEvent(Zombies.getInstance(), 0,
                map.getSpeedReapplyInterval()), map.getSpeedMaxLevel(), map.getSpeedDuration(),
                map.getSpeedAmplifier()));
        perks.put(PerkType.QUICK_FIRE, new QuickFire(player, map.getQuickFireMaxLevel()));
        perks.put(PerkType.EXTRA_HEALTH, new ExtraHealth(player, map.getExtraHealthMaxLevel(), map.getExtraHealthHpPerLevel()));
        perks.put(PerkType.EXTRA_WEAPON, new ExtraWeapon(player, map.getExtraHealthMaxLevel()));
        perks.put(PerkType.FAST_REVIVE, new FastRevive(player, map.getFastReviveMaxLevel()));
    }

    /**
     * Performs cleanup tasks.
     */
    @Override
    public void dispose() {
        for(Perk<?> perk : perks.values()) {
            perk.dispose();
        }
    }

    /**
     * Disables all perks.
     */
    public void disableAll() {
        for(Perk<?> perk : perks.values()) {
            perk.disable();
        }
    }

    public Perk<?> getPerk(PerkType type) {
        return perks.get(type);
    }
}
