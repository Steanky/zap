package io.github.zap.arenaapi.hotbar2;

import org.jetbrains.annotations.NotNull;

public interface HotbarGroupView {
    @NotNull HotbarProfile getProfile();

    void registerGrouping(@NotNull String groupName, int... slots);

    void appendObjectToGroup(@NotNull String groupName, @NotNull HotbarObject object);

    HotbarObject[] getObjectsFromGroup(@NotNull String groupName);

    int getCapacityOfGroup(@NotNull String groupName);

    int getSizeOfGroup(@NotNull String groupName);

    void removeObjectFromGroup(@NotNull String groupName, int groupIndex, boolean collapse);

    void redrawAllInGroup(@NotNull String groupName);
}
