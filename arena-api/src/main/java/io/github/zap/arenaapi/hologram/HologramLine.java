package io.github.zap.arenaapi.hologram;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.proxy.NMSProxy;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public abstract class HologramLine<V> {

    private final ProtocolManager protocolManager;

    private final NMSProxy nmsProxy;

    private final Location location;

    private final int id;

    private final Map<UUID, V> visualMap = new HashMap<>();

    private final PacketContainer spawnPacketContainer;

    private V defaultVisual;

    public HologramLine(Location location) {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.nmsProxy = ArenaApi.getInstance().getNmsProxy();
        this.location = location;
        this.id = nmsProxy.nextEntityId();
        this.spawnPacketContainer = createSpawnPacketContainer();
    }

    public void setVisualForEveryone(V visual) {
        setVisual(visual);
        visualMap.clear();
    }

    public void setVisual(V visual) {
        defaultVisual = visual;
    }

    public void setVisualForPlayer(Player player, V visual) {
        visualMap.put(player.getUniqueId(), visual);
    }

    public V getVisualForPlayer(Player player) {
        return visualMap.getOrDefault(player.getUniqueId(), defaultVisual);
    }

    public void createVisualForPlayer(Player player) {
        try {
            protocolManager.sendServerPacket(player, spawnPacketContainer);
        } catch (InvocationTargetException e) {
            ArenaApi.warning(
                    String.format(
                            "Error sending packet of type '%s'to player '%s'",
                            spawnPacketContainer.getType().name(),
                            player.getName()
                    )
            );
        }
    }

    protected abstract PacketContainer createSpawnPacketContainer();

    public abstract void updateVisualForPlayer(Player player);

}
