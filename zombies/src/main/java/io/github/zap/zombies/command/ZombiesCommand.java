package io.github.zap.zombies.command;

import io.github.regularcommands.commands.RegularCommand;
import io.github.zap.zombies.command.mapeditor.form.*;

/**
 * General command used by this plugin.
 */
public class ZombiesCommand extends RegularCommand {
    public ZombiesCommand() {
        super("zap");
        addForm(new MapLoaderProfilerForm());
        addForm(new ImportWorldForm());
        addForm(new JoinZombiesGameForm());
        addForm(new GiveWandForm());
        addForm(new MapCloseForm());
        addForm(new MapListForm());
        addForm(new NewMapForm());
        addForm(new NewRoomForm());
    }
}