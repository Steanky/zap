package io.github.zap.party.party;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartySettings {

    boolean ableToExpand = true;

    boolean allInvite = false;

    boolean anyoneCanJoin = false;

    long inviteExpirationTime = 1200L;

}
