package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.commands.RegularCommand;
import io.github.zap.zombies.command.mapeditor.form.*;

public class MapeditorCommand extends RegularCommand {
    public MapeditorCommand() {
        super("mapeditor");
        addForm(new EditMapForm());
        addForm(new GiveWandForm());
        addForm(new DeleteMapForm());
        addForm(new ListMapForm());
        addForm(new NewMapForm());
        addForm(new NewRoomForm());
        addForm(new NewWindowForm());
        addForm(new RemoveRoomForm());
        addForm(new WindowBoundsForm());
        addForm(new ExitEditorForm());
        addForm(new NewRoomSpawnpointForm());
        addForm(new NewWindowSpawnpointForm());
    }
}
