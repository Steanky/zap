package io.github.zap.arenaapi.nms.common.packet;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

@SuppressWarnings("ClassCanBeRecord")
public class ProtocolLibPacket implements Packet {

    private final static String FAILED_PACKET_MESSAGE = "An exception was thrown while sending a ProtocolLib packet.";

    private final ProtocolManager protocolManager;

    private final PacketContainer packetContainer;

    public ProtocolLibPacket(@NotNull ProtocolManager protocolManager, @NotNull PacketContainer packetContainer) {
        this.protocolManager = protocolManager;
        this.packetContainer = packetContainer;
    }

    @Override
    public void sendToPlayer(@NotNull Plugin plugin, @NotNull Player player) {
        try {
            protocolManager.sendServerPacket(player, packetContainer);
        } catch (InvocationTargetException e) {
            plugin.getLogger().log(Level.WARNING, FAILED_PACKET_MESSAGE, e);
        }
    }

}
