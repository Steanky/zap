package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Converters;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.RoomSelectionData;
import io.github.zap.zombies.command.mapeditor.form.data.WindowSelectionData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.map.SpawnpointData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.game.perk.PerkType;

import java.util.List;

public class NewWindowSpawnpointForm extends CommandForm<WindowSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("spawn"),
            new Parameter("create"),
            new Parameter(Regexes.NON_NEGATIVE_INTEGER, "[window-index]", Converters.INTEGER_CONVERTER),
            new Parameter(Regexes.OBJECT_NAME, "[rule-name]")
    };

    private static final CommandValidator<WindowSelectionData, RoomSelectionData> validator =
            new CommandValidator<>((context, arguments, previousData) -> {
        int index = (int)arguments[2];

        RoomData room = previousData.getRoom();
        List<WindowData> windows = room.getWindows();

        if(index >= windows.size()) {
            return ValidationResult.of(false, "That index is out of bounds!", null);
        }

        //noinspection SuspiciousMethodCalls
        if(!previousData.getMap().getSpawnRules().containsKey(arguments[3])) {
            return ValidationResult.of(false, "A spawnrule with that name does not exist!", null);
        }

        return ValidationResult.of(true, null, new WindowSelectionData(previousData.getPlayer(),
                previousData.getContext(), previousData.getSelection(), previousData.getMap(), previousData.getRoom(),
                windows.get(index)));
    }, MapeditorValidators.HAS_ROOM_SELECTION);

    public NewWindowSpawnpointForm() {
        super("Creates a new spawnpoint in a window.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<WindowSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, WindowSelectionData data) {
        EditorContext editorContext = data.getContext();
        data.getRoom().getSpawnpoints().add(new SpawnpointData(editorContext.getFirst(), editorContext.getSecond(),
                data.getWindow().getCenter(), (String)arguments[3]));
        editorContext.updateRenderable(EditorContext.Renderables.SPAWNPOINTS);
        return "Added spawnpoint to window.";
    }
}
