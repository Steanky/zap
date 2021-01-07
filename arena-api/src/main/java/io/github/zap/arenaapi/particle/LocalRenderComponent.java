package io.github.zap.arenaapi.particle;

import com.google.common.collect.Lists;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Localized render component. Renders only to specified player(s).
 */
public class LocalRenderComponent implements RenderComponent {
    private final String name;
    private Vector[] fragments;
    private final ParticleSettings settings;
    private final List<Player> players;

    public LocalRenderComponent(String name, ParticleSettings settings, List<Player> players, Vector... fragments) {
        this.name = name;
        this.fragments = fragments;
        this.settings = settings;
        this.players = players;
    }

    public LocalRenderComponent(String name, ParticleSettings settings, Player player, Vector... fragments) {
        this(name, settings, Lists.newArrayList(player), fragments);
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
        return players;
    }

    @Override
    public String name() {
        return name;
    }
}
