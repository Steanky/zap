package io.github.zap.zombies.game;

import io.github.zap.arenaapi.event.EmptyEventArgs;
import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.RepeatingEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedPerk extends Perk<EmptyEventArgs> {
    private final RepeatingEvent event;
    private final int effectDuration;
    private final int amplifier;

    public SpeedPerk(ZombiesPlayer owner, RepeatingEvent actionTriggerEvent, int effectDuration, int amplifier) {
        super(owner, actionTriggerEvent);

        event = actionTriggerEvent;
        this.effectDuration = effectDuration;
        this.amplifier = amplifier;
    }

    @Override
    public void execute(Event<EmptyEventArgs> event, EmptyEventArgs args) {
        getOwner().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, effectDuration, amplifier,
                true, false, false));
    }

    @Override
    public void activate() {
        super.activate();
        event.start();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        event.stop();

        getOwner().getPlayer().removePotionEffect(PotionEffectType.SPEED);
    }
}
