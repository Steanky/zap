package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.data.map.shop.tmtask.TeamMachineTask;
import io.github.zap.zombies.game.shop.ShopType;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a team machine
 */
@Getter
public class TeamMachineData extends BlockShopData {
    private final List<TeamMachineTask> teamMachineTasks = new ArrayList<>();

    private TeamMachineData() {
        super(ShopType.TEAM_MACHINE, true, null, null);

    }

    public TeamMachineData(Vector blockLocation, Vector hologramLocation) {
        super(ShopType.TEAM_MACHINE, true, blockLocation, hologramLocation);
    }
}
