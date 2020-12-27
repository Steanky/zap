package io.github.zap.zombies.game.shop;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentManager;
import io.github.zap.zombies.game.data.map.shop.LuckyChestData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LuckyChest extends ArmorStandShop<LuckyChestData> {

    private final List<EquipmentData<?>> equipments = new ArrayList<>();

    private int rolls = 0;

    private UUID rollingPlayerId = null;

    private boolean doneRolling = true;

    public LuckyChest(ZombiesArena zombiesArena, LuckyChestData shopData) {
        super(zombiesArena, shopData);

        EquipmentManager equipmentManager = zombiesArena.getEquipmentManager();
        String mapNameKey = getZombiesArena().getMap().getMapNameKey();
        for (String equipmentName : shopData.getEquipments()) {
            equipments.add(equipmentManager.getEquipmentData(mapNameKey, equipmentName));
        }
    }

    @Override
    public void displayTo(Player player) {

    }

    @Override
    public boolean purchase(ZombiesPlayer zombiesPlayer) {
        if (zombiesPlayer.getId().equals(rollingPlayerId)) {
            if (doneRolling) {
                // TODO: buy
                return true;
            } else {
                // TODO: not done
            }
        } else if (rollingPlayerId == null) {
            if (zombiesPlayer.getCoins() < getShopData().getCost()) {
                // TODO: poor
            } else {
                // TODO: start the chest

                rollingPlayerId = zombiesPlayer.getId();
                rolls++;
                doneRolling = false;

                return true;
            }
        } else {
            // TODO: someone else rolling
        }
        return false;
    }

    @Override
    public boolean shouldInteractWith(Object object) {
        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.LUCKY_CHEST.name();
    }
}
