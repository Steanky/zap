package io.github.zap.zombies.game2.corpse.message;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface CorpseMessager {

    void sendTimeUntilDeathMessage(@NotNull Player player, long timeUntilDeath);

    void sendTimeUntilRevivalMessage(@NotNull Player reviver, @NotNull Player revivee, long timeUntilRevival);

}
