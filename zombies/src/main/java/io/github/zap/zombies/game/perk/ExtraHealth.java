package io.github.zap.zombies.game.perk;

import io.github.zap.arenaapi.ObjectDisposedException;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesPlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

public class ExtraHealth extends MarkerPerk {
    private final int healthIncrement;

    public AttributeModifier currentMod;

    public ExtraHealth(ZombiesPlayer owner, int maxLevel, int healthIncrement, boolean resetLevelOnDisable) {
        super(owner, maxLevel, resetLevelOnDisable);
        this.healthIncrement = healthIncrement;
    }

    @Override
    public void activate() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        AttributeInstance attribute = getOwner().getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if(attribute != null) {
            if(currentMod != null) {
                attribute.removeModifier(currentMod);
            }

            currentMod = new AttributeModifier("extra thicc", getCurrentLevel() * healthIncrement,
                    AttributeModifier.Operation.ADD_NUMBER);
            attribute.addModifier(currentMod);
        }
        else {
            Zombies.warning("Could not get GenericAttributes.MAX_HEALTH for Player.");
        }
    }

    @Override
    public void deactivate() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        AttributeInstance attribute = getOwner().getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if(attribute != null && currentMod != null) {
            attribute.removeModifier(currentMod);
        }
    }
}
