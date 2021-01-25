package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public final class MapeditorValidators {
    public static final CommandValidator HAS_EDITOR_CONTEXT = new CommandValidator((context, arguments) -> {
        Player sender = (Player)context.getSender();
        Zombies zombies = Zombies.getInstance();

        if(!zombies.getContextManager().getContextMap().containsKey(sender.getUniqueId())) {
            return new ImmutablePair<>(false, "You are not currently editing a map.");
        }

        return new ImmutablePair<>(true, null);
    }, Validators.PLAYER_EXECUTOR);

    public static final CommandValidator NO_EDITOR_CONTEXT = new CommandValidator((context, arguments) -> {
        Player sender = (Player)context.getSender();
        Zombies zombies = Zombies.getInstance();

        if(zombies.getContextManager().getContextMap().containsKey(sender.getUniqueId())) {
            return new ImmutablePair<>(false, "You are already editing a map.");
        }

        return new ImmutablePair<>(true, null);
    }, Validators.PLAYER_EXECUTOR);

    public static final CommandValidator BOUNDS_REQUIRED = new CommandValidator((context, arguments) -> {
        Player sender = (Player)context.getSender();
        Zombies zombies = Zombies.getInstance();

        EditorContext editorContext = zombies.getContextManager().getContextMap().get(sender.getUniqueId());
        BoundingBox selectedBounds = editorContext.getSelectedBounds();

        if(selectedBounds == null) {
            return new ImmutablePair<>(false, "This command requires you to have selected at least two points!");
        }

        return new ImmutablePair<>(true, null);
    }, HAS_EDITOR_CONTEXT);
}
