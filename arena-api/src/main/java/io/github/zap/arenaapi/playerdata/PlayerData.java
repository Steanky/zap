package io.github.zap.arenaapi.playerdata;

import com.google.common.collect.ImmutableSet;

import java.util.Date;
import java.util.UUID;

public interface PlayerData {
    String getLocale();
    void setLocale(String locale);
    ImmutableSet<UUID> getFriends();
    boolean hasFriend(UUID id);
    void addFriend(UUID id);
    void removeFriend(UUID id);
    Date getLastLogin();
    void setLastLogin(Date date);
    RequestLevel getDirectMessageLevel();
    void setDirectMessageLevel(RequestLevel level);
    RequestLevel getFriendLevel();
    void setFriendLevel(RequestLevel level);
    RequestLevel getPartyLevel();
    void setPartyLevel(RequestLevel level);
    boolean isDirty();
}
