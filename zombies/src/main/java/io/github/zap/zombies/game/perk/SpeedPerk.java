package io.github.zap.zombies.game.perk;

import io.github.zap.arenaapi.event.EmptyEventArgs;
import io.github.zap.arenaapi.event.RepeatingEvent;
import io.github.zap.zombies.game.ZombiesPlayer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedPerk extends Perk<EmptyEventArgs> {
    private final RepeatingEvent event;
    private final int effectDuration;
    private final int baseAmplifier;

    public SpeedPerk(ZombiesPlayer owner, RepeatingEvent actionTriggerEvent, boolean resetOnQuit, int maxLevel,
                     int effectDuration, int baseAmplifier) {
        super(owner, actionTriggerEvent, maxLevel, resetOnQuit);

        event = actionTriggerEvent;
        this.effectDuration = effectDuration;
        this.baseAmplifier = baseAmplifier;
    }

    @Override
    public void execute(EmptyEventArgs args) {
        if(getOwner().isAlive()) {
            getOwner().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, effectDuration,
                    baseAmplifier * getCurrentLevel(), true, false, false));
        }
    }

    @Override
    public void activate() {
        event.start(); //redundant calls to start() are fine
    }

    @Override
    public void deactivate() {
        event.stop();
    }
}
