package io.github.zap.zombies.game.data.shop.tmtask;

import io.github.zap.zombies.game.shop.tmtask.AmmoSupply;
import io.github.zap.zombies.game.shop.tmtask.TeamMachineTaskType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


/**
 * Data for an {@link AmmoSupply} task
 */
public class AmmoSupplyData extends TeamMachineTaskData {

    public AmmoSupplyData(@NotNull String displayName, @NotNull List<@NotNull String> lore,
                          @NotNull Material displayMaterial, int initialCost) {
        super(TeamMachineTaskType.AMMO_SUPPLY.name(), displayName, lore, displayMaterial, initialCost);
    }

    public AmmoSupplyData() {
        super(TeamMachineTaskType.AMMO_SUPPLY.name(), "Ammo Supply", Collections.emptyList(), Material.ARROW, 1000);
    }

}
