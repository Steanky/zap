package io.github.zap.zombies.game.equipment.perk;

import io.github.zap.arenaapi.event.EmptyEventArgs;
import io.github.zap.arenaapi.event.RepeatingEvent;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.perk.PerkData;
import io.github.zap.zombies.game.data.equipment.perk.PerkLevel;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a perk hotbar equipment that involves a repeating event
 */
public abstract class RepeatingEventPerk<D extends @NotNull PerkData<L>, L extends @NotNull PerkLevel>
        extends Perk<D, L, RepeatingEvent, EmptyEventArgs> {

    public RepeatingEventPerk(@NotNull ZombiesPlayer player, int slot, @NotNull D perkData,
                              @NotNull RepeatingEvent actionTriggerEvent) {
        super(player, slot, perkData, actionTriggerEvent);
    }

    @Override
    public void activate() {
        if (getActionTriggerEvent() != null) {
            getActionTriggerEvent().start();
        }
    }

    @Override
    public void deactivate() {
        if (getActionTriggerEvent() != null) {
            getActionTriggerEvent().stop();
        }
    }

}
