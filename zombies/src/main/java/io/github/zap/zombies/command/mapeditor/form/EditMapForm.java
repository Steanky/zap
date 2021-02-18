package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.game.data.map.MapData;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;

public class EditMapForm extends CommandForm<Pair<MapData, Player>> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("edit"),
            new Parameter(Regexes.OBJECT_NAME, "[name]")
    };

    private static final CommandValidator<Pair<MapData, Player>, Player> validator =
            new CommandValidator<>((context, arguments, previous) -> {
        MapData map = Zombies.getInstance().getArenaManager().getMap((String)arguments[2]);

        if(map == null) {
            return ValidationResult.of(false, "That map does not exist!", null);
        }

        return ValidationResult.of(true, null, ImmutablePair.of(map, previous));
    }, Validators.PLAYER_EXECUTOR);

    public EditMapForm() {
        super("Edits an existing map.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<Pair<MapData, Player>, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, Pair<MapData, Player> data) {
        EditorContext editorContext = Zombies.getInstance().getContextManager().getContext(data.getRight());
        editorContext.setMap(data.getLeft());
        return String.format("Now editing map %s", data.getRight().getName());
    }
}
