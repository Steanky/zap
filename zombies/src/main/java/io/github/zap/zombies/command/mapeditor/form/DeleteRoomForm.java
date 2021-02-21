package io.github.zap.zombies.command.mapeditor.form;

import com.google.common.collect.Lists;
import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Converters;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.arenaapi.game.MultiBoundingBox;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.MapSelectionData;
import io.github.zap.zombies.command.mapeditor.form.data.RoomDeletionData;
import io.github.zap.zombies.game.data.map.RoomData;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class DeleteRoomForm extends CommandForm<RoomDeletionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("room"),
            new Parameter("remove"),
            new Parameter(Regexes.BOOLEAN, "[delete-all]", "false", Converters.BOOLEAN_CONVERTER,
                    Lists.newArrayList("true", "false"))
    };

    public DeleteRoomForm() {
        super("Removes a room.", Permissions.OPERATOR, parameters);
    }

    private static final CommandValidator<RoomDeletionData, MapSelectionData> validator =
            new CommandValidator<>((context, arguments, previousData) -> {
                boolean targetRoom = (boolean)arguments[2];

                BoundingBox selection = previousData.getSelection();
                List<Pair<RoomData, List<Integer>>> data = new ArrayList<>();

                int j = 0;
                for(RoomData room : previousData.getMap().getRooms()) {
                    MultiBoundingBox roomBounds = room.getBounds();

                    for(int i = 0; i < roomBounds.size(); i++) {
                        BoundingBox bounds = roomBounds.get(i);

                        if(bounds.overlaps(selection)) {
                            if(j >= data.size()) {
                                data.add(ImmutablePair.of(room, new ArrayList<>()));

                                if(targetRoom) {
                                    return ValidationResult.of(true, null, new RoomDeletionData(previousData.getPlayer(),
                                            previousData.getContext(), previousData.getSelection(), previousData.getMap(), data));
                                }
                            }

                            data.get(j).getRight().add(i);
                        }
                    }

                    j++;
                }

                if(data.size() > 0) {
                    return ValidationResult.of(true, null, new RoomDeletionData(previousData.getPlayer(),
                            previousData.getContext(), previousData.getSelection(), previousData.getMap(), data));
                }

                return ValidationResult.of(false, "No room or room bounds overlap that selection.", null);
            }, MapeditorValidators.HAS_MAP_SELECTION);

    @Override
    public CommandValidator<RoomDeletionData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, RoomDeletionData data) {
        if((boolean)arguments[2]) {
            RoomData target = data.getRoomIndices().get(0).getLeft();
            data.getMap().getRooms().remove(target);
            data.getContext().updateRenderable(EditorContext.Renderables.ROOMS);
            data.getContext().updateRenderable(EditorContext.Renderables.WINDOWS);
            return "Removed room '" + target.getName() + "'.";
        }

        List<Pair<RoomData, List<Integer>>> indices = data.getRoomIndices();

        int j = 0;
        for(Pair<RoomData, List<Integer>> pair : indices) {
            RoomData room = pair.getLeft();
            MultiBoundingBox bounds = room.getBounds();
            List<Integer> targets = pair.getRight();

            for(int i = targets.size() - 1; i > -1; i--) {
                bounds.remove(targets.get(i));
                j++;
            }

            if(bounds.size() == 0) {
                data.getMap().getRooms().remove(room);
            }
        }

        EditorContext editorContext = data.getContext();
        editorContext.updateRenderable(EditorContext.Renderables.ROOMS);
        editorContext.updateRenderable(EditorContext.Renderables.WINDOWS);
        editorContext.updateRenderable(EditorContext.Renderables.SPAWNPOINTS);
        editorContext.updateRenderable(EditorContext.Renderables.WINDOW_BOUNDS);

        return "Removed " + j + " bounds.";
    }
}
