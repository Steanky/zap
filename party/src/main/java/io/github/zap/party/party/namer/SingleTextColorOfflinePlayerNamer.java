package io.github.zap.party.party.namer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Names {@link OfflinePlayer}s based on their display name if they are online.
 * Otherwise, their string name is used alongside a {@link TextColor}.
 */
public class SingleTextColorOfflinePlayerNamer implements OfflinePlayerNamer {

    private final TextColor textColor;

    /**
     * Creates a single text offline player namer with a specified text color.
     * @param textColor The text color to use
     */
    public SingleTextColorOfflinePlayerNamer(@NotNull TextColor textColor) {
        this.textColor = textColor;
    }

    /**
     * Creates a single text offline player namer with a default text color.
     */
    public SingleTextColorOfflinePlayerNamer() {
        this(NamedTextColor.GRAY);
    }

    @Override
    public @NotNull Component name(@NotNull OfflinePlayer player) {
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null) {
            return onlinePlayer.displayName();
        }

        return Component.text(Objects.toString(player.getName()), this.textColor);
    }

}
