package io.github.zap.party.party;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Settings for a party. These can be modified by the owner.
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartySettings {

    boolean allInvite = false;

    boolean anyoneCanJoin = false;

    boolean muted = false;

    long inviteExpirationTime = 1200L;

}
