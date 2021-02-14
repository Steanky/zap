package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import org.bukkit.entity.Player;

public final class MapeditorValidators {
    public static final CommandValidator HAS_EDITOR_CONTEXT = new CommandValidator((context, form, arguments) -> {
        Zombies zombies = Zombies.getInstance();

        if(!zombies.getContextManager().hasContext((Player)context.getSender())) {
            return ValidationResult.of(false, "You need an editor session to use this command.");
        }

        return ValidationResult.of(true, null);
    }, Validators.PLAYER_EXECUTOR);

    public static final CommandValidator NO_ACTIVE_MAP = new CommandValidator((context, form, arguments) -> {
        if(Zombies.getInstance().getContextManager().getContext((Player)context.getSender()).getMap() != null) {
            return ValidationResult.of(false, "You are already editing a map.");
        }

        return ValidationResult.of(true, null);
    }, HAS_EDITOR_CONTEXT);

    public static final CommandValidator HAS_ACTIVE_MAP = new CommandValidator((context, form, arguments) -> {
        if(Zombies.getInstance().getContextManager().getContext(((Player)context.getSender())).getMap() == null) {
            return ValidationResult.of(false, "You are not editing a map.");
        }

        return ValidationResult.of(true, null);
    }, HAS_EDITOR_CONTEXT);

    public static final CommandValidator HAS_SELECTION = new CommandValidator((context, form, arguments) -> {
        if(Zombies.getInstance().getContextManager().getContext(((Player)context.getSender()))
                .getSelection() == null) {
            return ValidationResult.of(false, "You must have something selected to use this command.");
        }

        return ValidationResult.of(true, null);
    }, HAS_EDITOR_CONTEXT);
}
