package io.github.zap.zombies.command;

import io.github.regularcommands.commands.RegularCommand;
import io.github.zap.zombies.command.mapeditor.NewMapForm;

/**
 * Command that should be used for generic testing of things during development. Add forms to this as necessary.
 */
public class ZombiesCommand extends RegularCommand {
    public ZombiesCommand() {
        super("zap");
        addForm(new MapLoaderProfilerForm());
        addForm(new ImportWorldForm());
        addForm(new JoinZombiesGameForm());
        addForm(new NewMapForm());
    }
}
