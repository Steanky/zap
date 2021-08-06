package io.github.zap.party.party;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;
import java.util.Optional;
import java.util.UUID;

/**
 * A member of a party
 */
public class PartyMember {

    private final Server server;

    private final UUID playerUUID;

    private SoftReference<OfflinePlayer> cachedPlayer;

    private boolean inPartyChat = false;

    private boolean muted = false;

    public PartyMember(@NotNull Player player) {
        this.server = player.getServer();
        this.playerUUID = player.getUniqueId();
        this.cachedPlayer = new SoftReference<>(player);
    }

    /**
     * Gets the associated {@link Player} with this member if they are online.
     * This should be used in contrast to {@link OfflinePlayer#getPlayer()} from {@link #getOfflinePlayer()}
     * for better caching
     * @return An optional of the player
     */
    public @NotNull Optional<Player> getPlayerIfOnline() {
        OfflinePlayer player = cachedPlayer.get();

        if (player != null) {
            if (player.isOnline()) {
                return Optional.ofNullable(player.getPlayer());
            }
            else {
                this.cachedPlayer.clear();
            }
        }
        else {
            Player fresh = server.getPlayer(playerUUID);

            if (fresh != null && fresh.isOnline()) {
                this.cachedPlayer = new SoftReference<>(fresh);
                return Optional.of(fresh);
            }
        }

        return Optional.empty();
    }

    /**
     * Gets the {@link OfflinePlayer} associated with this member
     * @return The offline player
     */
    public @NotNull OfflinePlayer getOfflinePlayer() {
        OfflinePlayer player = this.cachedPlayer.get();
        if (player != null) {
            return player;
        }
        else {
            OfflinePlayer fresh = this.server.getOfflinePlayer(this.playerUUID);
            this.cachedPlayer = new SoftReference<>(fresh);
            return fresh;
        }
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
