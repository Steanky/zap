package io.github.zap.arenaapi.nms.common.packet;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class MultiPacket implements Packet {

    private final Packet[] packets;

    public MultiPacket(@NotNull Packet... packets) {
        this.packets = packets;
    }

    @Override
    public void sendToPlayer(@NotNull Plugin plugin, @NotNull Player player) {
        for (Packet packet : packets) {
            packet.sendToPlayer(plugin, player);
        }
    }

}
