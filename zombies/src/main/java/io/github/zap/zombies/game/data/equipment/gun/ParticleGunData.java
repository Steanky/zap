package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.util.ParticleDataWrapper;
import lombok.Getter;
import org.bukkit.Particle;

/**
 * Data for a gun associated with a particle
 * @param <L> The gun level type
 */
@Getter
public class ParticleGunData<L extends ParticleGunLevel> extends GunData<L> {

    private Particle particle;

    private ParticleDataWrapper<?> particleData;

}
