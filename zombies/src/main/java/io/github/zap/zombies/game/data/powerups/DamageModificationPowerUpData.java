package io.github.zap.zombies.game.data.powerups;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;

@Getter
@Setter
public class DamageModificationPowerUpData extends DurationPowerUpData {
    boolean isInstaKill;
    double multiplier;
    double additionalDamage;
}
