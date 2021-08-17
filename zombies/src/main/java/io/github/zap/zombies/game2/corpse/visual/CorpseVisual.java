package io.github.zap.zombies.game2.corpse.visual;

import io.github.zap.arenaapi.hotbar2.PlayerView;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface CorpseVisual {

    void renderToPlayer(@NotNull PlayerView player);

}
