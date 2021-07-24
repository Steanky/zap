package io.github.zap.zombies.game.data.shop.tmtask;

import io.github.zap.zombies.game.shop.tmtask.TeamMachineTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic implementation of a {@link TeamMachineTaskCreator}
 */
public class BasicTeamMachineTaskCreator implements TeamMachineTaskCreator {

    private final @NotNull Map<@NotNull String, @NotNull TeamMachineTaskMapping<@NotNull ?>> teamMachineTaskMappings = new HashMap<>();

    public BasicTeamMachineTaskCreator(@NotNull List<@NotNull TeamMachineTaskDataMappingPair<@NotNull ?, @NotNull ?>> shopMappings) {
        for (@NotNull TeamMachineTaskDataMappingPair<@NotNull ?, @NotNull ?> shopDataMappingPair : shopMappings) {
            this.teamMachineTaskMappings.put(shopDataMappingPair.data().getType(), shopDataMappingPair.mapping());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <D extends @NotNull TeamMachineTaskData> TeamMachineTask<D> createTeamMachineTask(@NotNull D teamMachineTaskData) {
        TeamMachineTaskMapping<@NotNull ?> mapping = teamMachineTaskMappings.get(teamMachineTaskData.getType());
        return (mapping != null) ? ((TeamMachineTaskMapping<D>) mapping).createTeamMachineTask(teamMachineTaskData) : null;
    }

}
