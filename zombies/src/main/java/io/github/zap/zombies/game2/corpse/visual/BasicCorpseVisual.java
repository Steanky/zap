package io.github.zap.zombies.game2.corpse.visual;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar2.PlayerView;
import io.github.zap.arenaapi.nms.common.packet.Packet;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class BasicCorpseVisual implements CorpseVisual {

    private final Plugin plugin;

    private final Hologram hologram;

    private final Packet addPlayerPacket;

    private final Packet removePlayerPacket;

    private final Packet spawnPlayerPacket;

    private final Packet armorPacket;

    private final Packet sleepingPacket;

    private final Packet teamPacket;

    private final long tabListDelay;

    public BasicCorpseVisual(@NotNull Plugin plugin, @NotNull Hologram hologram, @NotNull Packet addPlayerPacket,
                             @NotNull Packet removePlayerPacket, @NotNull Packet spawnPlayerPacket,
                             @NotNull Packet armorPacket, @NotNull Packet sleepingPacket, @NotNull Packet teamPacket,
                             long tabListDelay) {
        this.plugin = plugin;
        this.hologram = hologram;
        this.addPlayerPacket = addPlayerPacket;
        this.removePlayerPacket = removePlayerPacket;
        this.spawnPlayerPacket = spawnPlayerPacket;
        this.armorPacket = armorPacket;
        this.sleepingPacket = sleepingPacket;
        this.teamPacket = teamPacket;
        this.tabListDelay = tabListDelay;
    }

    @Override
    public void renderToPlayer(@NotNull PlayerView player) {
        player.getPlayerIfValid().ifPresent(bukkitPlayer -> {
            addPlayerPacket.sendToPlayer(plugin, bukkitPlayer);
            spawnPlayerPacket.sendToPlayer(plugin, bukkitPlayer);
            armorPacket.sendToPlayer(plugin, bukkitPlayer);
            sleepingPacket.sendToPlayer(plugin, bukkitPlayer);
            teamPacket.sendToPlayer(plugin, bukkitPlayer);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.getPlayerIfValid().ifPresent(nextBukkitPlayer -> {
                    removePlayerPacket.sendToPlayer(plugin, nextBukkitPlayer);
                });
            }, tabListDelay);
        });
    }
}
