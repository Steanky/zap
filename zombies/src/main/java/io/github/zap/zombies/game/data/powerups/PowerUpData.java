package io.github.zap.zombies.game.data.powerups;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;

@Getter
@Setter
public class PowerUpData {
    String type;
    String name;
    String powerUpType = ChatColor.GOLD + "ads";

    String displayName;
    Material itemRepresentation = Material.GOLD_BLOCK;
    int itemCount = 1;

    // In ticks
    int despawnDuration = 6000;
    double pickupRange = 1;

    Sound pickupSound = Sound.ENTITY_ITEM_PICKUP;

    float pickupSoundVolume = 1;

    float pickupSoundPitch = 1;
}
