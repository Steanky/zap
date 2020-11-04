package io.github.zap.command;

import io.github.regularcommands.commands.RegularCommand;

/**
 * Command that should be used for generic testing of things during development. Add forms to this as necessary.
 */
public class DebugCommand extends RegularCommand {
    public DebugCommand() {
        super("zap_debug");
        addForm(new MapLoaderProfilerForm());
    }
}
