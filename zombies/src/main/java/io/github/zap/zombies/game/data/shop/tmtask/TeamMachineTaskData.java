package io.github.zap.zombies.game.data.shop.tmtask;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TeamMachineTaskData {

    private final @NotNull String type;

    private final @NotNull String displayName;

    private final @NotNull List<@NotNull String> lore;

    private final @NotNull Material displayMaterial;

    private final int initialCost;

    public TeamMachineTaskData(@NotNull String type, @NotNull String displayName, @NotNull List<@NotNull String> lore,
                               @NotNull Material displayMaterial, int initialCost) {
        this.type = type;
        this.displayName = displayName;
        this.lore = lore;
        this.displayMaterial = displayMaterial;
        this.initialCost = initialCost;
    }


    public @NotNull String getType() {
        return type;
    }

    public @NotNull String getDisplayName() {
        return displayName;
    }

    public @NotNull List<@NotNull String> getLore() {
        return lore;
    }

    public @NotNull Material getDisplayMaterial() {
        return displayMaterial;
    }

    public int getInitialCost() {
        return initialCost;
    }
}
