package io.github.zap.arenaapi.particle;

import org.bukkit.util.Vector;

public abstract class BasicRenderComponent implements RenderComponent {
    private final String name;
    private final ParticleSettings settings;

    protected Vector[] cache = null;

    public BasicRenderComponent(String name, ParticleSettings settings) {
        this.name = name;
        this.settings = settings;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Vector[] getVectors() {
        if(cache == null) {
            updateVectors();
        }

        return cache;
    }

    @Override
    public ParticleSettings getSettings() {
        return settings;
    }
}
