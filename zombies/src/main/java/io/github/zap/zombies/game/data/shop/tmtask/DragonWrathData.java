package io.github.zap.zombies.game.data.shop.tmtask;

import io.github.zap.zombies.game.shop.tmtask.DragonWrath;
import io.github.zap.zombies.game.shop.tmtask.TeamMachineTaskType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Data for a {@link DragonWrath} task
 */
public class DragonWrathData extends TeamMachineTaskData {

    private int costIncrement = 1000;

    private int delay = 30;

    private double radius = 15D;

    public DragonWrathData(@NotNull String displayName, @NotNull List<@NotNull String> lore,
                           @NotNull Material displayMaterial, int initialCost, int costIncrement, int delay,
                           double radius) {
        super(TeamMachineTaskType.DRAGON_WRATH.name(), displayName, lore, displayMaterial, initialCost);

        this.costIncrement = costIncrement;
        this.delay = delay;
        this.radius = radius;
    }

    public DragonWrathData() {
        super(TeamMachineTaskType.DRAGON_WRATH.name(), "Dragon Wrath", Collections.emptyList(), Material.DRAGON_EGG, 2000);
    }

    public int getCostIncrement() {
        return costIncrement;
    }

    public int getDelay() {
        return delay;
    }

    public double getRadius() {
        return radius;
    }

}
