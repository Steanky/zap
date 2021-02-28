package io.github.zap.zombies.game.perk;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.event.RepeatingEvent;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.MapData;
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
        boolean resetOnQuit = map.isPerksLostOnQuit();

        perks.put(PerkType.SPEED, new SpeedPerk(player, new RepeatingEvent(Zombies.getInstance(), 0,
                map.getSpeedReapplyInterval()), resetOnQuit, map.getSpeedMaxLevel(), map.getSpeedDuration(),
                map.getSpeedAmplifier()));
        perks.put(PerkType.QUICK_FIRE, new QuickFire(player, map.getQuickFireMaxLevel(), resetOnQuit));
        perks.put(PerkType.EXTRA_HEALTH, new ExtraHealth(player, map.getExtraHealthMaxLevel(),
                map.getExtraHealthHpPerLevel(), resetOnQuit));
        perks.put(PerkType.EXTRA_WEAPON, new ExtraWeapon(player, map.getExtraHealthMaxLevel(), resetOnQuit));
        perks.put(PerkType.FAST_REVIVE, new FastRevive(player, map.getFastReviveMaxLevel(), resetOnQuit,
                map.getDefaultReviveTime(), map.getTickReductionPerLevel()));
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

    /**
     * Activates all perk effects that should be applied, considering their current level.
     */
    public void activateAll() {
        for(Perk<?> perk : perks.values()) {
            perk.activate();
        }
    }

    public Perk<?> getPerk(PerkType type) {
        return perks.get(type);
    }
}
