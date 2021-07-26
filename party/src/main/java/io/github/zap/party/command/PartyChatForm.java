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
import io.github.zap.party.party.PartyMember;
import org.bukkit.entity.Player;

public class PartyChatForm extends CommandForm<Void> {

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("chat")
    };

    private static final CommandValidator<Void, ?> VALIDATOR
            = new CommandValidator<>((context, arguments, previousData) -> {
        Party party = PartyPlusPlus.getInstance().getPartyManager().getPartyForPlayer(previousData);

        if (party == null) {
            return ValidationResult.of(false, "You are not currently in a party.", null);
        }

        return ValidationResult.of(true, null, null);
    }, Validators.PLAYER_EXECUTOR);

    public PartyChatForm() {
        super("Toggles party chat.", Permissions.NONE, PARAMETERS);
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

        PartyMember partyMember = PartyPlusPlus.getInstance().getPartyManager().getPlayerAsPartyMember(sender);
        if (partyMember != null) {
            partyMember.setInPartyChat(!partyMember.isInPartyChat());
            return String.format(">gold{Turned party chat %s!}", (partyMember.isInPartyChat()) ? "ON" : "OFF");
        }

        return null;
    }
}
