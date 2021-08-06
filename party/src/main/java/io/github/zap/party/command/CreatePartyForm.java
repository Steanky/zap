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
import io.github.zap.party.party.PartyManager;
import io.github.zap.party.party.PartyMember;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Creates a new party
 */
public class CreatePartyForm extends CommandForm<Void> {

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("create")
    };

    private static final CommandValidator<Void, ?> VALIDATOR
            = new CommandValidator<>((context, arguments, previousData) -> {
        Optional<Party> partyOptional = PartyPlusPlus.getInstance().getPartyManager()
                .getPartyForPlayer((OfflinePlayer) context.getSender());

        if (partyOptional.isEmpty()) {
            return ValidationResult.of(false, "You are already in a party.", null);
        }

        return ValidationResult.of(true, null, null);
    }, Validators.PLAYER_EXECUTOR);

    public CreatePartyForm() {
        super("Creates a party.", Permissions.NONE, PARAMETERS);
    }

    @Override
    public boolean canStylize() {
        return true;
    }

    @Override
    public CommandValidator<Void, ?> getValidator(Context context, Object[] arguments) {
        return VALIDATOR;
    }

    @Override
    public String execute(Context context, Object[] arguments, Void data) {
        Player sender = (Player) context.getSender();
        PartyManager partyManager = PartyPlusPlus.getInstance().getPartyManager();
        partyManager.createParty(new PartyMember(sender), partyManager.createPartySettings(sender));

        return ">gold{Created a new party.}";
    }

}
