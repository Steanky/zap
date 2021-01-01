package io.github.zap.zombies.command;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.commands.PermissionData;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.validator.CommandValidator;

public class MapCreationForm extends CommandForm {
    public MapCreationForm(PermissionData permissionData, Parameter... parameters) {
        super("usage", permissionData, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return null;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        return null;
    }
}