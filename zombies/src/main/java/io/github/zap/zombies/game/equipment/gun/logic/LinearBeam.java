package io.github.zap.zombies.game.equipment.gun.logic;

import com.google.common.collect.Sets;
import io.github.zap.zombies.game.data.equipment.gun.LinearGunLevel;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.Set;

/**
 * Sends lines of particles from guns
 */
@Getter
public class LinearBeam extends BasicBeam { // TODO: figuring out particle data

    public final static int DEFAULT_PARTICLE_COUNT = 4; // TODO: check

    private final static Set<Material> AIR_MATERIALS =
            Sets.newHashSet(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);

    private final Particle particle;
    private final int particleCount;


    public LinearBeam(Location root, Particle particle, LinearGunLevel level, int particleCount) {
        super(root, level);

        this.particle = particle;
        this.particleCount = particleCount;
    }

    public LinearBeam(Location root, Particle particle, LinearGunLevel level) {
        this(root, particle, level, DEFAULT_PARTICLE_COUNT);
    }

    /**
     * Sends the bullet
     */
    public void send() {
        super.send();
        spawnParticles();
    }

    /**
     * Spawns the bullet's particles in a line
     */
    private void spawnParticles() {
        World world = getWorld();
        Location rootLocation = getRoot().toLocation(world);

        for (int i = 0; i < particleCount; i++) {
            world.spawnParticle(
                    particle,
                    rootLocation,
                    0,
                    0,
                    0,
                    0
            );
            rootLocation.add(getDirectionVector());
        }
    }

}
