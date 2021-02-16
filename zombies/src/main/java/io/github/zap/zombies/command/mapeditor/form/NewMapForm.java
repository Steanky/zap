package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class NewMapForm extends CommandForm<BoundingBox> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("create"),
            new Parameter(Regexes.OBJECT_NAME, "[name]")
    };

    private static final CommandValidator<BoundingBox, BoundingBox> validator = new CommandValidator<>((context, form, previousData) -> {
        if(Zombies.getInstance().getArenaManager().hasMap((String)arguments[2])) {
            return ValidationResult.of(false, "A map with that name already exists.");
        }

        return ValidationResult.of(true, null);
    }, MapeditorValidators.HAS_SELECTION);

    public NewMapForm() {
        super("Creates a new map.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        Zombies zombies = Zombies.getInstance();
        Player player = (Player)context.getSender();
        String mapName = (String)arguments[2];
        String worldName = player.getWorld().getName();

        EditorContext editorContext = zombies.getContextManager().getContext(player);
        MapData map = new MapData(mapName, worldName, editorContext.getSelection());
        zombies.getArenaManager().addMap(map);
        editorContext.setMap(map);

        return String.format("Created new map '%s' in world %s", mapName, worldName);
    }
}
