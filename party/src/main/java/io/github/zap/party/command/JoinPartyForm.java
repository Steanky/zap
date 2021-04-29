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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Joins a player's party if it exists
 */
public class JoinPartyForm extends CommandForm<Party> {

    private static final Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("join"),
            new Parameter("\\w+", "[owner-name]")
    };

    private static final CommandValidator<Party, ?> VALIDATOR
            = new CommandValidator<>(((context, arguments, previousData) -> {
        Player player = (Player) context.getSender();
        PartyManager partyManager = PartyPlusPlus.getInstance().getPartyManager();

        if (partyManager.getPartyForPlayer(player) != null) {
            return ValidationResult.of(false,
                    "You are already in a party! Leave it to join another one.", null);
        }

        String ownerName = (String) arguments[1];
        if (context.getSender().getName().equals(ownerName)) {
            return ValidationResult.of(false, "You cannot join your own party.", null);
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayerIfCached(ownerName);
        if (owner == null) {
            return ValidationResult.of(false, String.format("%s is registered on the server!", ownerName),
                    null);
        }

        ownerName = owner.getName(); // change any capitalization

        Party party = partyManager.getPartyForPlayer(owner);

        if (party == null) {
            return ValidationResult.of(false, String.format("%s is not in a party.", owner), null);
        }

        if (!(party.hasInvite(player) || party.getPartySettings().isAnyoneCanJoin())) {
            return ValidationResult.of(false, String.format("You don't have an invite to %s's party!",
                    ownerName), null);
        }

        return ValidationResult.of(true, null, party);
    }), Validators.PLAYER_EXECUTOR);

    public JoinPartyForm() {
        super("Joins a party.", Permissions.NONE, PARAMETERS);
    }

    @Override
    public CommandValidator<Party, ?> getValidator(Context context, Object[] arguments) {
        return VALIDATOR;
    }

    @Override
    public String execute(Context context, Object[] arguments, Party data) {
        PartyPlusPlus.getInstance().getPartyManager().addPlayerToParty(data, (OfflinePlayer) context.getSender());
        return null;
    }

}
