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
import io.github.zap.party.party.PartySettings;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Invites a player to your party
 */
public class InvitePlayerForm extends CommandForm<Player> {

    private final static Parameter[] PARAMETERS = new Parameter[] {
            new Parameter("invite"),
            new Parameter("\\w+", "[player-name]")
    };

    private final static CommandValidator<Player, ?> VALIDATOR
            = new CommandValidator<>((context, arguments, previousData) -> {
        String playerName = (String) arguments[1];

        if (previousData.getName().equalsIgnoreCase(playerName)) {
            return ValidationResult.of(false, "You cannot join your own party.", null);
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return ValidationResult.of(false, String.format("%s is currently not online.", playerName), null);
        }

        return ValidationResult.of(true, null, player);
    }, Validators.PLAYER_EXECUTOR);

    private final PartyPlusPlus partyPlusPlus;

    private final MiniMessage miniMessage;

    public InvitePlayerForm(@NotNull PartyPlusPlus partyPlusPlus, @NotNull MiniMessage miniMessage) {
        super("Invites a player to your party.", Permissions.NONE, PARAMETERS);
        this.partyPlusPlus = partyPlusPlus;
        this.miniMessage = miniMessage;
    }

    @Override
    public CommandValidator<Player, ?> getValidator(Context context, Object[] arguments) {
        return VALIDATOR;
    }

    @Override
    public String execute(Context context, Object[] arguments, Player data) {
        Player sender = (Player) context.getSender();

        Party party = partyPlusPlus.getPartyForPlayer(sender).orElseGet(() -> {
            Party newParty = new Party(partyPlusPlus, miniMessage, new PartyMember(sender),
                    new PartySettings(), PartyMember::new);
            partyPlusPlus.trackParty(newParty);

            return newParty;
        });

        if (party.isOwner(sender) || party.getPartySettings().isAllInvite()) {
            party.invitePlayer(data, sender);
            return null;
        }

        return ">red{You do not have permission to invite players!}";
    }

}
