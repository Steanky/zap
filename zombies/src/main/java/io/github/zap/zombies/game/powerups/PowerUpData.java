package io.github.zap.zombies.game.powerups;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import org.bukkit.Material;
import org.bukkit.Sound;

@Getter
@Setter
public class PowerUpData {
    String type;
    String name;
    String powerUpType;

    String displayName;
    Material itemRepresentation;

    // In ticks
    int despawnDuration = 6000;
    double pickupRange = 1;

    Sound pickupSound = Sound.ENTITY_ITEM_PICKUP;

    float pickupSoundVolume = 1;

    float pickupSoundPitch = 1;
}
