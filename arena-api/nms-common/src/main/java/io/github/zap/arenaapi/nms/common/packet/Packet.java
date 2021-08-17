package io.github.zap.arenaapi.nms.common.packet;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface Packet {

    void sendToPlayer(@NotNull Plugin plugin, @NotNull Player player);

}
