package io.github.zap.arenaapi.playerdata;

import com.google.common.collect.ImmutableSet;
import io.github.zap.arenaapi.serialize.DataSerializable;
import io.github.zap.arenaapi.serialize.Serialize;
import io.github.zap.arenaapi.serialize.TypeAlias;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@TypeAlias("PlayerData")
public class FilePlayerData extends DataSerializable implements PlayerData {
    Set<UUID> friends = new HashSet<>();

    @Getter
    Date lastLogin = new Date();

    @Getter
    RequestLevel friendLevel = RequestLevel.EVERYONE;

    @Getter
    RequestLevel partyLevel = RequestLevel.EVERYONE;

    @Getter
    RequestLevel directMessageLevel = RequestLevel.EVERYONE;

    @Getter
    @Serialize(skip = true)
    boolean isDirty = false;

    public FilePlayerData() {}

    @Override
    public ImmutableSet<UUID> getFriends() {
        return ImmutableSet.copyOf(friends);
    }

    @Override
    public boolean hasFriend(UUID id) {
        return friends.contains(id);
    }

    @Override
    public void addFriend(UUID id) {
        if(friends.add(id)) {
            isDirty = true;
        }
    }

    @Override
    public void removeFriend(UUID id) {
        if(friends.remove(id)) {
            isDirty = true;
        }
    }

    @Override
    public void setLastLogin(Date date) {
        if(date != lastLogin) {
            lastLogin = date;
            isDirty = true;
        }
    }

    @Override
    public void setFriendLevel(RequestLevel level) {
        if(level != friendLevel) {
            friendLevel = level;
            isDirty = true;
        }
    }

    @Override
    public void setPartyLevel(RequestLevel level) {
        if(level != partyLevel) {
            partyLevel = level;
            isDirty = true;
        }
    }

    @Override
    public void setDirectMessageLevel(RequestLevel level) {
        if(level != directMessageLevel) {
            directMessageLevel = level;
            isDirty = true;
        }
    }
}
