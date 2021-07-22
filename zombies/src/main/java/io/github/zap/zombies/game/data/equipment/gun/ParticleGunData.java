package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.zombies.game.util.ParticleDataWrapper;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Data for a gun associated with a particle
 * @param <L> The gun level type
 */
public abstract class ParticleGunData<L extends @NotNull ParticleGunLevel> extends GunData<L> {

    private final Particle particle;

    private final ParticleDataWrapper<?> particleDataWrapper;

    public ParticleGunData(@NotNull String type, @NotNull String name, @NotNull String displayName,
                           @NotNull Material material, @NotNull List<String> lore, @NotNull List<L> levels,
                           @NotNull Particle particle, @Nullable ParticleDataWrapper<?> particleDataWrapper) {
        super(type, name, displayName, material, lore, levels);

        this.particle = particle;
        this.particleDataWrapper = particleDataWrapper;
    }

    protected ParticleGunData() {
        this.particle = Particle.CRIT;
        this.particleDataWrapper = null;
    }

    /**
     * Gets the particle this gun uses
     * @return The particle
     */
    public @NotNull Particle getParticle() {
        return particle;
    }

    // TODO: @see
    /**
     * Gets the particle data wrapper this gun uses depending on its particle
     * {@see #getParticle()}
     * @return The data wrapper
     */
    public @Nullable ParticleDataWrapper<?> getParticleDataWrapper() {
        return particleDataWrapper;
    }

}
