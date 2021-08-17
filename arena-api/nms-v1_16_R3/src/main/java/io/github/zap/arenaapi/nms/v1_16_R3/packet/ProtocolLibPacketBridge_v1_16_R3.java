package io.github.zap.arenaapi.nms.v1_16_R3.packet;

import com.comphenix.protocol.ProtocolManager;
import io.github.zap.arenaapi.nms.common.packet.PacketBridge;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class ProtocolLibPacketBridge_v1_16_R3 implements PacketBridge {

    private final ProtocolManager protocolManager;

    public ProtocolLibPacketBridge_v1_16_R3(@NotNull ProtocolManager protocolManager) {
        this.protocolManager = protocolManager;
    }

}
