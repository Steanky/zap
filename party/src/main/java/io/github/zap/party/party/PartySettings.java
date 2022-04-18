package io.github.zap.party.party;

/**
 * Settings for a party. These can be modified by the owner.
 */
public class PartySettings {

    private boolean allInvite = false;

    private boolean anyoneCanJoin = false;

    private boolean muted = false;

    private long inviteExpirationTime = 1200L;

    /**
     * Gets whether all players can invite other players
     * @return Whether all players can invite others
     */
    public boolean isAllInvite() {
        return this.allInvite;
    }

    /**
     * Sets whether all players can invite other players
     * @param allInvite Whether all players can invite other players
     */
    public void setAllInvite(boolean allInvite) {
        this.allInvite = allInvite;
    }

    /**
     * Gets whether anyone can join the party without an invite
     * @return Whether anyone can join the party without an invite
     */
    public boolean isAnyoneCanJoin() {
        return this.anyoneCanJoin;
    }

    /**
     * Sets whether anyone can join the party without an invite
     * @param anyoneCanJoin Whether anyone can join the party without an invite
     */
    public void setAnyoneCanJoin(boolean anyoneCanJoin) {
        this.anyoneCanJoin = anyoneCanJoin;
    }

    /**
     * Gets whether the party is muted and no players can speak
     * @return Whether the party is muted and no players can speak
     */
    public boolean isMuted() {
        return this.muted;
    }

    /**
     * Sets whether the party is muted and no players can speak
     * @param muted whether the party is muted and no players can speak
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    /**
     * Gets the expiration time of an invite
     * @return The expiration time of an invite
     */
    public long getInviteExpirationTime() {
        return this.inviteExpirationTime;
    }

    /**
     * Sets the expiration time of an invite
     * @param inviteExpirationTime The expiration time of an invite
     */
    public void setInviteExpirationTime(long inviteExpirationTime) {
        this.inviteExpirationTime = inviteExpirationTime;
    }

}
