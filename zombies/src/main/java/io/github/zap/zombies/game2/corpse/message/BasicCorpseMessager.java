package io.github.zap.zombies.game2.corpse.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class BasicCorpseMessager implements CorpseMessager {

    private final MiniMessage miniMessage;

    public BasicCorpseMessager(@NotNull MiniMessage miniMessage) {
        this.miniMessage = miniMessage;
    }

    @Override
    public void sendTimeUntilDeathMessage(@NotNull Player player, long timeUntilDeath) {
        player.sendMessage(miniMessage.parse("<red>You will die in <reset><time><reset><red>!",
                Template.of("time", convertTicksToTimeComponent(timeUntilDeath))));
    }

    @Override
    public void sendTimeUntilRevivalMessage(@NotNull Player reviver, @NotNull Player revivee, long timeUntilRevival) {
        Template time = Template.of("time", convertTicksToTimeComponent(timeUntilRevival));
        reviver.sendMessage(miniMessage.parse("<red>Reviving <reset><revivee><reset><red>..." +
                " <white>- <reset><time><reset><red>!", Template.of("revivee", reviver.displayName()), time));
        revivee.sendMessage(miniMessage.parse("<red>You are being revived by <reset><reviver><reset><red>!" +
                " <white>- <reset><time><reset><red>!", Template.of("reviver", reviver.displayName()), time));
    }

    private @NotNull Component convertTicksToTimeComponent(long ticks) {
        return miniMessage.parse(String.format("<yellow>%.2fs", (double) (ticks / 20) + 0.05D * (ticks % 20)));
    }

}
