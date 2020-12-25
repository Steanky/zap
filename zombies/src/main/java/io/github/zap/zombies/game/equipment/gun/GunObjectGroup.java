package io.github.zap.zombies.game.equipment.gun;

import io.github.zap.zombies.game.hotbar.HotbarObjectGroup;
import org.bukkit.entity.Player;

import java.util.Set;

public class GunObjectGroup extends HotbarObjectGroup {

    public GunObjectGroup(Player player, Set<Integer> slots) {
        super(player, slots);
    }

    @Override
    public void remove(int slotId, boolean replace) {
        super.remove(slotId, replace);
        if (replace) {

        }
    }
}
