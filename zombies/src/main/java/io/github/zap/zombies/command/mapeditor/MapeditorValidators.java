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

        if(!zombies.getContextManager().hasContext((Player)context.getSender())) {
            return new ImmutablePair<>(false, "You need an editor session to use this command.");
        }

        return new ImmutablePair<>(true, null);
    }, Validators.PLAYER_EXECUTOR);

    public static final CommandValidator NO_EDITOR_CONTEXT = new CommandValidator((context, arguments) -> {
        Player sender = (Player)context.getSender();
        Zombies zombies = Zombies.getInstance();

        if(zombies.getContextManager().hasContext((Player)context.getSender())) {
            return new ImmutablePair<>(false, "You must not have an active editor session to use this command.");
        }

        return new ImmutablePair<>(true, null);
    }, Validators.PLAYER_EXECUTOR);

    public static final CommandValidator SELECTION_REQUIRED = new CommandValidator((context, arguments) -> {
        Player sender = (Player)context.getSender();
        Zombies zombies = Zombies.getInstance();

        EditorContext editorContext = zombies.getContextManager().fetchContext((Player)context.getSender());
        BoundingBox selectedBounds = editorContext.getSelectedBounds();

        if(selectedBounds == null) {
            return new ImmutablePair<>(false, "This command requires you to have selected at least one point!");
        }

        return new ImmutablePair<>(true, null);
    }, HAS_EDITOR_CONTEXT);

    public static final CommandValidator POINT_REQUIRED = new CommandValidator((context, arguments) -> {
        EditorContext editorContext = Zombies.getInstance().getContextManager().fetchContext((Player)context.getSender());

        if(!editorContext.getFirstClicked().equals(editorContext.getSecondClicked())) {
            return new ImmutablePair<>(false, "This command requires you to select a single block!");
        }

        return new ImmutablePair<>(true, null);
    }, SELECTION_REQUIRED);

    public static final CommandValidator BOUNDS_REQUIRED = new CommandValidator((context, arguments) -> {
        EditorContext editorContext = Zombies.getInstance().getContextManager().fetchContext((Player)context.getSender());

        if(editorContext.getFirstClicked().equals(editorContext.getSecondClicked())) {
            return new ImmutablePair<>(false, "This command requires you to select a bounds rather than a" +
                    " single block!");
        }

        return new ImmutablePair<>(true, null);
    }, SELECTION_REQUIRED);

    public static CommandValidator NO_ACTIVE_MAP = new CommandValidator((context, arguments) -> {
        if(Zombies.getInstance().getContextManager().fetchContext((Player)context.getSender()).getEditingMap() != null) {
            return ImmutablePair.of(false, "You are already editing a map.");
        }

        return ImmutablePair.of(true, null);
    }, MapeditorValidators.HAS_EDITOR_CONTEXT);

    public static CommandValidator HAS_ACTIVE_MAP = new CommandValidator((context, arguments) -> {
        if(Zombies.getInstance().getContextManager().fetchContext(((Player)context.getSender()))
                .getEditingMap() == null) {
            return ImmutablePair.of(false, "You are not editing a map.");
        }

        return ImmutablePair.of(true, null);
    }, MapeditorValidators.HAS_EDITOR_CONTEXT);

    public static CommandValidator mapExistsValidator(int nameParameter, CommandValidator depend) {
        return new CommandValidator((context, arguments) -> {
            if(!Zombies.getInstance().getArenaManager().hasMap((String)arguments[nameParameter])) {
                return ImmutablePair.of(false, "That map does not exist.");
            }

            return ImmutablePair.of(true, null);
        }, depend);
    }

    public static CommandValidator noMapExistsValidator(int nameParameter, CommandValidator depend) {
        return new CommandValidator((context, arguments) -> {
            if(Zombies.getInstance().getArenaManager().hasMap((String)arguments[nameParameter])) {
                return ImmutablePair.of(false, "That map already exists.");
            }

            return ImmutablePair.of(true, null);
        }, depend);
    }
}
