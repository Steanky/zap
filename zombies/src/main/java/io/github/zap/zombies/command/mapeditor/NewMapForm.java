package io.github.zap.zombies.command.mapeditor;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.map.MapData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class NewMapForm extends CommandForm {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("map"),
            new Parameter("create"),
            new Parameter("^(\\w+)$", "[map_name]")
    };

    public NewMapForm() {
        super("Create a new Zombies map.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator getValidator(Context context, Object[] arguments) {
        return MapeditorValidators.HAS_EDITOR_CONTEXT;
    }

    @Override
    public String execute(Context context, Object[] arguments) {
        String name = (String)arguments[2];
        Player player = (Player)context.getSender();
        String worldName = player.getWorld().getName();

        Zombies zombies = Zombies.getInstance();
        zombies.getContextManager().getContextMap().get(player.getUniqueId()).setEditingMap(new MapData(name, worldName));

        PlayerInventory inventory = player.getInventory();

        for(ItemStack item : inventory) {

        }

        return String.format("Now editing new map '%s' for world '%s'.", name, worldName);
    }
}