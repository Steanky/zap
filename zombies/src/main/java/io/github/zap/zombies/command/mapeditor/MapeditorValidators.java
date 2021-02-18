package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.form.data.BoundsContextData;
import io.github.zap.zombies.command.mapeditor.form.data.EditorContextData;
import io.github.zap.zombies.command.mapeditor.form.data.MapContextData;
import io.github.zap.zombies.command.mapeditor.form.data.MapSelectionData;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public final class MapeditorValidators {
    public static final CommandValidator<EditorContextData, Player> HAS_EDITOR_CONTEXT =
            new CommandValidator<>((context, arguments, previousData) -> {
        Zombies zombies = Zombies.getInstance();
        EditorContext editorContext = zombies.getContextManager().getContext(previousData);

        if(editorContext == null) {
            return ValidationResult.of(false, "You need an editor session to use this command.", null);
        }

        return ValidationResult.of(true, null, new EditorContextData(previousData, editorContext));
    }, Validators.PLAYER_EXECUTOR);

    public static final CommandValidator<EditorContextData, EditorContextData> NO_ACTIVE_MAP =
            new CommandValidator<>((context, arguments, previousData) -> {
        if(previousData.getContext().getMap() != null) {
            return ValidationResult.of(false, "You are already editing a map.", null);
        }

        return ValidationResult.of(true, null, previousData);
    }, HAS_EDITOR_CONTEXT);

    public static final CommandValidator<MapContextData, EditorContextData>
            HAS_ACTIVE_MAP = new CommandValidator<>((context, arguments, previousData) -> {
        MapData map = previousData.getContext().getMap();
        if(map == null) {
            return ValidationResult.of(false, "You are not editing a map.", null);
        }

        return ValidationResult.of(true, null, new MapContextData(previousData.getPlayer(), previousData.getContext(), map));
    }, HAS_EDITOR_CONTEXT);

    public static final CommandValidator<BoundsContextData, EditorContextData>
            HAS_SELECTION = new CommandValidator<>((context, arguments, previousData) -> {
        BoundingBox selection = previousData.getContext().getSelection();
        if(selection == null) {
            return ValidationResult.of(false, "You must have something selected to use this command.", null);
        }

        return ValidationResult.of(true, null, new BoundsContextData(previousData.getPlayer(), previousData.getContext(), selection));
    }, HAS_EDITOR_CONTEXT);

    public static final CommandValidator<MapSelectionData, BoundsContextData> HAS_MAP_SELECTION =
            new CommandValidator<>((context, arguments, previousData) ->
                    ValidationResult.of(true, null, new MapSelectionData(previousData.getPlayer(),
                            previousData.getContext(), previousData.getBounds(), previousData.getContext().getMap())),
                    HAS_SELECTION.from(HAS_ACTIVE_MAP));
}
