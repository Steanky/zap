package io.github.zap.zombies.game2.arena;

import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.zombies.game2.arena.player.HiddenPlayersManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ZombiesArena extends Arena<ZombiesArena> {

    private final HiddenPlayersManager hiddenPlayersManager;

    public ZombiesArena(@NotNull ArenaManager<ZombiesArena> manager, @NotNull World world,
                        @NotNull HiddenPlayersManager hiddenPlayersManager) {
        super(manager, world);

        this.hiddenPlayersManager = hiddenPlayersManager;
    }

    @Override
    public boolean handleJoin(List<Player> list) {
        return false;
    }

    @Override
    public void handleLeave(List<Player> list) {

    }

    @Override
    public boolean hasPlayer(UUID uuid) {
        return false;
    }

    @Override
    public boolean isPlayerPlaying(UUID uuid) {
        return false;
    }

    @Override
    public void dispose() {

    }

}
