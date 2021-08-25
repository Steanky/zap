package io.github.zap.party.party.invitation;

import io.github.zap.party.party.Party;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

/**
 * Manages a {@link io.github.zap.party.party.Party}'s invitations
 */
public interface InvitationManager {

    /**
     * Determines whether a player has an invitation to this party
     * @param player The potentially invited player
     * @return Whether the player has an invitation
     */
    boolean hasInvitation(@NotNull OfflinePlayer player);

    /**
     * Gets a copy of the set of invitations for this manager.
     * @return A copy of the set of invitations
     */
    @NotNull Set<UUID> getInvitations();

    /**
     * Adds an invitation.
     * @param party The party to add the invitation for
     * @param invitee The invitation invitee
     * @param inviter The invitation inviter
     */
    void addInvitation(@NotNull Party party, @NotNull OfflinePlayer invitee, @NotNull OfflinePlayer inviter);

    /**
     * Removes an invitation.
     * @param player The player whose invite should be removed
     * @return Whether removal was successful
     */
    @SuppressWarnings("UnusedReturnValue")
    boolean removeInvitation(@NotNull OfflinePlayer player);

    /**
     * Cancels all outgoing invitations.
     */
    void cancelAllOutgoingInvitations();

}
