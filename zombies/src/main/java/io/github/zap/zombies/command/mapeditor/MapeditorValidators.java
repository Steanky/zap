package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public final class MapeditorValidators {
    public static final CommandValidator<EditorContext, Player> HAS_EDITOR_CONTEXT = new CommandValidator<>((context, arguments, previousData) -> {
        Zombies zombies = Zombies.getInstance();
        EditorContext editorContext = zombies.getContextManager().getContext(previousData);

        if(editorContext == null) {
            return ValidationResult.of(false, "You need an editor session to use this command.", null);
        }

        return ValidationResult.of(true, null, editorContext);
    }, Validators.PLAYER_EXECUTOR);

    public static final CommandValidator<EditorContext, EditorContext> NO_ACTIVE_MAP = new CommandValidator<>((context, arguments, previousData) -> {
        if(previousData.getMap() != null) {
            return ValidationResult.of(false, "You are already editing a map.", null);
        }

        return ValidationResult.of(true, null, previousData);
    }, HAS_EDITOR_CONTEXT);

    public static final CommandValidator<MapData, EditorContext> HAS_ACTIVE_MAP = new CommandValidator<>((context, form, previousData) -> {
        if(previousData.getMap() == null) {
            return ValidationResult.of(false, "You are not editing a map.", null);
        }

        return ValidationResult.of(true, null, previousData.getMap());
    }, HAS_EDITOR_CONTEXT);

    public static final CommandValidator<BoundingBox, EditorContext> HAS_SELECTION = new CommandValidator<>((context, form, previousData) -> {
        BoundingBox selection = previousData.getSelection();
        if(selection == null) {
            return ValidationResult.of(false, "You must have something selected to use this command.", null);
        }

        return ValidationResult.of(true, null, selection);
    }, HAS_EDITOR_CONTEXT);
}
