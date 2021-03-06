package io.github.zap.zombies.game.data.powerups;

import io.github.zap.zombies.game.data.util.ItemStackDescription;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;

/**
 * The base class of all power up data
 */
@Getter
@Setter
public class PowerUpData {
    String type;
    String name;
    String powerUpType;

    ItemStackDescription itemRepresentation;
    String displayName;

    // In ticks
    int despawnDuration = 6000;
    double pickupRange = 1;

    Sound pickupSound = Sound.ENTITY_ITEM_PICKUP;

    float pickupSoundVolume = 1;

    float pickupSoundPitch = 1;
}