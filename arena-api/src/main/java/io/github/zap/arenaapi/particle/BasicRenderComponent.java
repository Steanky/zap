package io.github.zap.arenaapi.particle;

import io.github.zap.arenaapi.ParameterlessAction;
import org.bukkit.util.Vector;

import java.util.function.Function;

/**
 * Basic RenderComponent implementation.
 */
public class BasicRenderComponent implements RenderComponent {
    private final String name;
    private Vector[] fragments;
    private final ParticleSettings settings;
    private final ParameterlessAction<Vector[]> updateFunction;

    public BasicRenderComponent(String name, ParticleSettings settings, ParameterlessAction<Vector[]> updateFunction,
                                Vector... initialFragments) {
        this.name = name;
        this.settings = settings;
        this.fragments = initialFragments;
        this.updateFunction = updateFunction;
    }

    @Override
    public Vector[] getFragments() {
        return fragments;
    }

    @Override
    public void updateFragments() {
        this.fragments = updateFunction.invoke();
    }

    @Override
    public ParticleSettings particleData() {
        return settings;
    }

    @Override
    public String name() {
        return name;
    }
}
