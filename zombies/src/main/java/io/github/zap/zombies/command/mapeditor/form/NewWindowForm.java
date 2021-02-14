package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.commands.PermissionData;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.validator.CommandValidator;

public class NewWindowForm extends CommandForm {
    public NewWindowForm(String usage, PermissionData permissionData, Parameter... parameters) {
        super(usage, permissionData, parameters);
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
