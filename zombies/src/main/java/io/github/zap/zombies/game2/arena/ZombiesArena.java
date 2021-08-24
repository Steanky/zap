package io.github.zap.zombies.game2.arena;

import io.github.zap.arenaapi.game.arena.Arena;
import io.github.zap.arenaapi.game.arena.ArenaManager;
import io.github.zap.zombies.game2.arena.hiddenplayer.HiddenPlayers;
import io.github.zap.zombies.game2.arena.player.PlayerList;
import io.github.zap.zombies.game2.player.ZombiesPlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ZombiesArena extends Arena<ZombiesArena> {

    private final PlayerList<ZombiesPlayer> players;

    private final HiddenPlayers hiddenPlayers;

    public ZombiesArena(@NotNull ArenaManager<ZombiesArena> manager, @NotNull World world,
                        @NotNull PlayerList<ZombiesPlayer> players, @NotNull HiddenPlayers hiddenPlayers) {
        super(manager, world);

        this.players = players;
        this.hiddenPlayers = hiddenPlayers;
    }

    public void tick() {
        for (ZombiesPlayer player : players.getPlayers().values()) {
            player.tick();
        }
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
