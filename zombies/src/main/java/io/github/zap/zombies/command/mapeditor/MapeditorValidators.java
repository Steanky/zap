package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public final class MapeditorValidators {
    public static final CommandValidator HAS_EDITOR_CONTEXT = new CommandValidator((context, form, arguments) -> {
        Zombies zombies = Zombies.getInstance();

        if(!zombies.getContextManager().hasContext((Player)context.getSender())) {
            return ValidationResult.of(false, "You need an editor session to use this command.");
        }

        return ValidationResult.of(true, null);
    }, Validators.PLAYER_EXECUTOR);

    public static final CommandValidator NO_EDITOR_CONTEXT = new CommandValidator((context, form, arguments) -> {
        Zombies zombies = Zombies.getInstance();

        if(zombies.getContextManager().hasContext((Player)context.getSender())) {
            return ValidationResult.of(false, "You must not have an active editor session to use this command.");
        }

        return ValidationResult.of(true, null);
    }, Validators.PLAYER_EXECUTOR);

    public static final CommandValidator SELECTION_REQUIRED = new CommandValidator((context, form, arguments) -> {
        Zombies zombies = Zombies.getInstance();

        EditorContext editorContext = zombies.getContextManager().fetchContext((Player)context.getSender());
        BoundingBox selectedBounds = editorContext.getSelectedBounds();

        if(selectedBounds == null) {
            return ValidationResult.of(false, "This command requires you to have selected at least one point!");
        }

        return ValidationResult.of(true, null);
    }, HAS_EDITOR_CONTEXT);

    public static final CommandValidator POINT_REQUIRED = new CommandValidator((context, form, arguments) -> {
        EditorContext editorContext = Zombies.getInstance().getContextManager().fetchContext((Player)context.getSender());

        if(!editorContext.getFirstClicked().equals(editorContext.getSecondClicked())) {
            return ValidationResult.of(false, "This command requires you to select a single block!");
        }

        return ValidationResult.of(true, null);
    }, SELECTION_REQUIRED);

    public static final CommandValidator BOUNDS_REQUIRED = new CommandValidator((context, form, arguments) -> {
        EditorContext editorContext = Zombies.getInstance().getContextManager().fetchContext((Player)context.getSender());

        if(editorContext.getFirstClicked().equals(editorContext.getSecondClicked())) {
            return ValidationResult.of(false, "This command requires you to select a bounds rather than a" +
                    " single block!");
        }

        return ValidationResult.of(true, null);
    }, SELECTION_REQUIRED);

    public static CommandValidator NO_ACTIVE_MAP = new CommandValidator((context, form, arguments) -> {
        if(Zombies.getInstance().getContextManager().fetchContext((Player)context.getSender()).getEditingMap() != null) {
            return ValidationResult.of(false, "You are already editing a map.");
        }

        return ValidationResult.of(true, null);
    }, HAS_EDITOR_CONTEXT);

    public static CommandValidator HAS_ACTIVE_MAP = new CommandValidator((context, form, arguments) -> {
        if(Zombies.getInstance().getContextManager().fetchContext(((Player)context.getSender()))
                .getEditingMap() == null) {
            return ValidationResult.of(false, "You are not editing a map.");
        }

        return ValidationResult.of(true, null);
    }, HAS_EDITOR_CONTEXT);

    public static CommandValidator HAS_MAP_AND_BOUNDS_SELECTION = new CommandValidator(BOUNDS_REQUIRED.getStep(), HAS_ACTIVE_MAP);

    public static CommandValidator newMapExistsValidator(int nameParameter, CommandValidator depend) {
        return new CommandValidator((context, form, arguments) -> {
            if(!Zombies.getInstance().getArenaManager().hasMap((String)arguments[nameParameter])) {
                return ValidationResult.of(false, "That map does not exist.");
            }

            return ValidationResult.of(true, null);
        }, depend);
    }

    public static CommandValidator mapAbsentValidator(int nameParameter, CommandValidator depend) {
        return new CommandValidator((context, form, arguments) -> {
            if(Zombies.getInstance().getArenaManager().hasMap((String)arguments[nameParameter])) {
                return ValidationResult.of(false, "That map already exists.");
            }

            return ValidationResult.of(true, null);
        }, depend);
    }
}
