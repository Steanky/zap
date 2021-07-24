package io.github.zap.zombies.game.data.shop.tmtask;

import io.github.zap.zombies.game.shop.tmtask.FullRevive;
import io.github.zap.zombies.game.shop.tmtask.TeamMachineTaskType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Data for a {@link FullRevive} task
 */
public class FullReviveData extends TeamMachineTaskData {

    public FullReviveData(@NotNull String displayName, @NotNull List<@NotNull String> lore,
                          @NotNull Material displayMaterial, int initialCost) {
        super(TeamMachineTaskType.FULL_REVIVE.name(), displayName, lore, displayMaterial, initialCost);
    }

    public FullReviveData() {
        super(TeamMachineTaskType.FULL_REVIVE.name(), "Full Revive", Collections.emptyList(), Material.GOLDEN_APPLE, 2000);
    }

}
