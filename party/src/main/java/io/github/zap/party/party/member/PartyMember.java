package io.github.zap.party.party.member;

import io.github.zap.party.party.Party;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * A member of a {@link Party}.
 */
public class PartyMember {

    private final Server server;

    private final UUID playerUUID;

    private Player player;

    private int captureTick;

    private boolean inPartyChat = false;

    private boolean muted = false;

    /**
     * Creates a party member.
     * @param player The player this party member initially manages.
     */
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
     * Gets the {@link OfflinePlayer} associated with this member.
     * @return The offline player
     */
    public @NotNull OfflinePlayer getOfflinePlayer() {
        Optional<Player> playerOptional = this.getPlayerIfOnline();

        if (playerOptional.isPresent()) {
            return playerOptional.get();
        }

        return this.server.getOfflinePlayer(this.playerUUID);
    }

    /**
     * Gets whether the member is currently in party chat.
     * Party messages will be routed to a specific party chat if this returns {@code true}.
     * @return Whether the member is currently in party chat
     */
    public boolean isInPartyChat() {
        return this.inPartyChat;
    }

    /**
     * Sets whether the member is currently in party chat.
     * @param inPartyChat Whether the member should now be in party chat
     */
    public void setInPartyChat(boolean inPartyChat) {
        this.inPartyChat = inPartyChat;
    }

    /**
     * Gets whether the member is muted.
     * {@link io.github.zap.party.party.chat.PartyChatHandler}s should block messages if this returns {@code true}.
     * @return Whether the member is currently muted
     */
    public boolean isMuted() {
        return this.muted;
    }

    /**
     * Sets whether the member is muted.
     * @param muted Whether the member should now be muted
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

}
