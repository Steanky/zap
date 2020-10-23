package io.github.zap.command;

import io.github.regularcommands.commands.RegularCommand;

public class MapeditorCommand extends RegularCommand {
    public MapeditorCommand() {
        super("mapeditor");
        addForm(new MapCreationForm());
    }
}
