package io.github.zap.zombies.game;

import io.github.zap.arenaapi.event.EmptyEventArgs;
import io.github.zap.arenaapi.event.RepeatingEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedPerk extends Perk<EmptyEventArgs> {
    private final RepeatingEvent event;
    private final int effectDuration;
    private final int baseAmplifier;

    public SpeedPerk(ZombiesPlayer owner, RepeatingEvent actionTriggerEvent, int maxLevel, int effectDuration,
                     int baseAmplifier) {
        super(owner, actionTriggerEvent, maxLevel);

        event = actionTriggerEvent;
        this.effectDuration = effectDuration;
        this.baseAmplifier = baseAmplifier;
    }

    @Override
    public void execute(EmptyEventArgs args) {
        getOwner().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, effectDuration,
                baseAmplifier * getCurrentLevel(), true, false, false));
    }

    @Override
    public boolean upgrade() {
        if(super.upgrade()) {
            if(getCurrentLevel() == 1) {
                event.start();
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean downgrade() {
        if(super.downgrade()) {
            if(getCurrentLevel() == 0) {
                event.stop();
                getOwner().getPlayer().removePotionEffect(PotionEffectType.SPEED);
            }

            return true;
        }

        return false;
    }
}
