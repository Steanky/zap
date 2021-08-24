package io.github.zap.party.party;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * A member of a party
 */
public class PartyMember {

    private final Server server;

    private final UUID playerUUID;

    private Player player;

    private int captureTick;

    private boolean inPartyChat = false;

    private boolean muted = false;

    public PartyMember(@NotNull Player player) {
        this.server = player.getServer();
        this.playerUUID = player.getUniqueId();
        this.player = player;
        this.captureTick = player.getServer().getCurrentTick();
    }

    /**
     * Gets the associated {@link Player} with this member if they are online.
     * @return An optional of the player
     */
    public @NotNull Optional<Player> getPlayerIfOnline() {
        int currentTick = this.server.getCurrentTick();
        if (currentTick == this.captureTick) {
            return Optional.ofNullable(this.player);
        }

        this.captureTick = currentTick;
        return Optional.ofNullable(this.player = this.server.getPlayer(this.playerUUID));
    }

    /**
     * Gets the {@link OfflinePlayer} associated with this member
     * @return The offline player
     */
    public @NotNull OfflinePlayer getOfflinePlayer() {
        Optional<Player> playerOptional = this.getPlayerIfOnline();

        if (playerOptional.isPresent()) {
            return playerOptional.get();
        }

        return this.server.getOfflinePlayer(this.playerUUID);
    }

    public boolean isInPartyChat() {
        return this.inPartyChat;
    }

    public void setInPartyChat(boolean inPartyChat) {
        this.inPartyChat = inPartyChat;
    }

    public boolean isMuted() {
        return this.muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

}
