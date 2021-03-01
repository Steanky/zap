package io.github.zap.arenaapi.hologram;

import com.comphenix.protocol.events.PacketContainer;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.proxy.NMSProxy;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A line of a hologram
 * @param <V> The type of the displayed visual
 */
@Getter
public abstract class HologramLine<V> {

    private final ArenaApi arenaApi;

    private final NMSProxy nmsProxy;

    private final Location location;

    private final int entityId;

    private final Map<UUID, V> visualMap = new HashMap<>();

    private final PacketContainer spawnPacketContainer;

    private V defaultVisual;

    public HologramLine(Location location) {
        this.arenaApi = ArenaApi.getInstance();
        this.nmsProxy = arenaApi.getNmsProxy();
        this.location = location;
        this.entityId = nmsProxy.nextEntityId();
        this.spawnPacketContainer = createSpawnPacketContainer();

        for (Player player : location.getWorld().getPlayers()) {
            createVisualForPlayer(player);
        }
    }

    /**
     * Sets the visual of the hologram for all players, overriding player specific visuals
     * @param visual The new visual
     */
    public void setVisualForEveryone(V visual) {
        visualMap.clear();
        setVisual(visual);
    }

    /**
     * Sets the default visual of the hologram for all players
     * @param visual The new visual
     */
    public void setVisual(V visual) {
        defaultVisual = visual;

        for (Player player : location.getWorld().getPlayers()) {
            if (!visualMap.containsKey(player.getUniqueId())) {
                updateVisualForPlayer(player);
            }
        }
    }

    /**
     * Sets the visual for a single player
     * @param player The player to set the visual for
     * @param visual The new visual
     */
    public void setVisualForPlayer(Player player, V visual) {
        visualMap.put(player.getUniqueId(), visual);
        updateVisualForPlayer(player);
    }

    /**
     * Gets the visual for a player
     * @param player The player to get the visual for
     * @return The visual the player sees
     */
    public V getVisualForPlayer(Player player) {
        return visualMap.getOrDefault(player.getUniqueId(), defaultVisual);
    }

    /**
     * Spawns the visual for a player
     * @param player The player to spawn the visual for
     */
    public void createVisualForPlayer(Player player) {
        ArenaApi.getInstance().sendPacketToPlayer(player, spawnPacketContainer);
    }

    /**
     * Creates a packet container which creates the hologram line in minecraft
     * @return The packet container
     */
    protected abstract PacketContainer createSpawnPacketContainer();

    /**
     * Updates the visual for a player
     * @param player The player to update for
     */
    public abstract void updateVisualForPlayer(Player player);

}
