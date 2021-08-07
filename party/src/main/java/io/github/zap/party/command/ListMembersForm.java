package io.github.zap.party.command;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.party.PartyPlusPlus;
import io.github.zap.party.party.Party;
import net.kyori.adventure.text.Component;

import java.util.Optional;

/**
 * Lists all members in a party
 */
public class ListMembersForm extends CommandForm<Party> {

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("list")
    };

    private static final CommandValidator<Party, ?> VALIDATOR
            = new CommandValidator<>((context, arguments, previousData) -> {
        Optional<Party> partyOptional = PartyPlusPlus.getInstance().getPartyForPlayer(previousData);
        if (partyOptional.isEmpty()) {
            return ValidationResult.of(false, "You are not currently in a party.", null);
        }

        return ValidationResult.of(true, null, partyOptional.get());
    }, Validators.PLAYER_EXECUTOR);

    public ListMembersForm() {
        super("Lists all members in your party.", Permissions.NONE, PARAMETERS);
    }

    @Override
    public CommandValidator<Party, ?> getValidator(Context context, Object[] arguments) {
        return VALIDATOR;
    }

    @Override
    public String execute(Context context, Object[] arguments, Party data) {
        for (Component component : data.getPartyListComponents()) {
            context.getSender().sendMessage(component);
        }

        return null;
    }

}
