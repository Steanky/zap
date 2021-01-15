package io.github.zap.arenaapi.particle;

import org.bukkit.util.Vector;

public interface RenderComponent {
    String getName();

    Vector[] getVectors();

    ParticleSettings getSettings();

    void updateVectors();
}
