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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Invites a player to your party
 */
public class InvitePlayerForm extends CommandForm<Player> {

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("invite"),
            new Parameter("\\w+", "[player-name]")
    };

    private static final CommandValidator<Player, ?> VALIDATOR
            = new CommandValidator<>((context, arguments, previousData) -> {
        String playerName = (String) arguments[1];
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            return ValidationResult.of(false, String.format("%s is currently not online.", playerName), null);
        }

        return ValidationResult.of(true, null, player);
    }, Validators.PLAYER_EXECUTOR);

    public InvitePlayerForm() {
        super("Invites a player to your party.", Permissions.NONE, PARAMETERS);
    }

    @Override
    public CommandValidator<Player, ?> getValidator(Context context, Object[] arguments) {
        return VALIDATOR;
    }

    @Override
    public String execute(Context context, Object[] arguments, Player data) {
        PartyManager partyManager = PartyPlusPlus.getInstance().getPartyManager();

        Player sender = (Player) context.getSender();
        Party party = partyManager.getPartyForPlayer(sender);

        if (party == null) {
            party = partyManager.createParty(sender);
        }

        if (party.getOwner().getPlayer().equals(sender) || party.getPartySettings().isAllInvite()) {
            partyManager.invitePlayer(party, sender, data);
            return null;
        }

        return ">red{You do not have permission to invite players!}";
    }

}
