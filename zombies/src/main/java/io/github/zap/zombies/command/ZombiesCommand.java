package io.github.zap.zombies.command;

import io.github.regularcommands.commands.RegularCommand;
import io.github.zap.zombies.command.mapeditor.EditMapForm;
import io.github.zap.zombies.command.mapeditor.GiveWandForm;
import io.github.zap.zombies.command.mapeditor.NewMapForm;
import io.github.zap.zombies.command.mapeditor.NewSessionForm;

/**
 * General command used by this plugin.
 */
public class ZombiesCommand extends RegularCommand {
    public ZombiesCommand() {
        super("zap");
        addForm(new MapLoaderProfilerForm());
        addForm(new ImportWorldForm());
        addForm(new JoinZombiesGameForm());

        addForm(new NewSessionForm());
        addForm(new NewMapForm());
        addForm(new EditMapForm());
        addForm(new GiveWandForm());
    }
}
