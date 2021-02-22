package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.data.map.shop.tmtask.TeamMachineTask;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a team machine
 */
@Getter
public class TeamMachineData extends BlockShopData {

    private List<TeamMachineTask> teamMachineTasks = new ArrayList<>();

    public TeamMachineData(Vector blockLocation, Vector hologramLocation) {
        super(blockLocation, hologramLocation);
    }
}
