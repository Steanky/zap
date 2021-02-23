package io.github.zap.arenaapi.hologram;

import com.comphenix.protocol.events.PacketContainer;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.arenaapi.proxy.NMSProxy;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public abstract class HologramLine<V> {

    private final LocalizationManager localizationManager;

    private final ArenaApi arenaApi;

    private final NMSProxy nmsProxy;

    private final Location location;

    private final int entityId;

    private final Map<UUID, V> visualMap = new HashMap<>();

    private final PacketContainer spawnPacketContainer;

    private V defaultVisual;

    public HologramLine(LocalizationManager localizationManager, Location location) {
        this.localizationManager = localizationManager;
        this.arenaApi = ArenaApi.getInstance();
        this.nmsProxy = arenaApi.getNmsProxy();
        this.location = location;
        this.entityId = nmsProxy.nextEntityId();
        this.spawnPacketContainer = createSpawnPacketContainer();

        for (Player player : location.getWorld().getPlayers()) {
            createVisualForPlayer(player);
        }
    }

    public void setVisualForEveryone(V visual) {
        visualMap.clear();
        setVisual(visual);
    }

    public void setVisual(V visual) {
        defaultVisual = visual;

        for (Player player : location.getWorld().getPlayers()) {
            if (!visualMap.containsKey(player.getUniqueId())) {
                updateVisualForPlayer(player);
            }
        }
    }

    public void setVisualForPlayer(Player player, V visual) {
        visualMap.put(player.getUniqueId(), visual);
        updateVisualForPlayer(player);
    }

    public V getVisualForPlayer(Player player) {
        return visualMap.getOrDefault(player.getUniqueId(), defaultVisual);
    }

    public void createVisualForPlayer(Player player) {
        ArenaApi.getInstance().sendPacketToPlayer(player, spawnPacketContainer);
    }

    protected abstract PacketContainer createSpawnPacketContainer();

    public abstract void updateVisualForPlayer(Player player);

}
