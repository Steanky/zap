package io.github.zap.zombies.game.data.equipment;

import io.github.zap.zombies.game.equipment.Equipment;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class EquipmentCreator {

    @Getter
    private final Map<String, EquipmentMapping<?, ?>> equipmentMappings = new HashMap<>();

    public interface EquipmentMapping<D extends EquipmentData<L>, L> {
        Equipment<D, L> createEquipment(Player player, int slotId, D equipmentData);
    }

}
