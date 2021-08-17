package io.github.zap.zombies.game2.arena.item;

import org.bukkit.entity.Item;
import org.jetbrains.annotations.NotNull;

public interface ProtectedItems {

    void addItem(@NotNull Item item);

    void removeItem(@NotNull Item item);

}
