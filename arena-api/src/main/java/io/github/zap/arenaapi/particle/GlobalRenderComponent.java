package io.github.zap.arenaapi.particle;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * RenderComponent implementation; renders to all players in a world.
 */
public class GlobalRenderComponent implements RenderComponent {
    private final String name;
    private Vector[] fragments;
    private final ParticleSettings settings;
    private final World world;

    public GlobalRenderComponent(String name, ParticleSettings settings, World world, Vector... fragments) {
        this.name = name;
        this.fragments = fragments;
        this.settings = settings;
        this.world = world;
    }

    @Override
    public Vector[] getFragments() {
        return fragments;
    }

    @Override
    public void updateFragments(Vector[] fragments) {
        this.fragments = fragments;
    }

    @Override
    public ParticleSettings particleData() {
        return settings;
    }

    @Override
    public List<Player> renderTo() {
        return world.getPlayers();
    }

    @Override
    public String name() {
        return name;
    }
}
