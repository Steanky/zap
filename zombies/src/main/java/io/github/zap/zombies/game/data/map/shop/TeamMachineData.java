package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.zombies.game.data.map.shop.tmtask.TeamMachineTask;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TeamMachineData extends BlockShopData {

    private List<TeamMachineTask> teamMachineTasks = new ArrayList<>();

    private TeamMachineData() {

    }

}
