package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.arenaapi.serialize.JacksonDataLoader;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.tmtask.TeamMachineTask;
import io.github.zap.zombies.game.data.map.shop.tmtask.TeamMachineTaskDeserializer;
import io.github.zap.zombies.game.shop.*;
import lombok.Getter;

/**
 * Class for storing and managing shop data using Jackson's data loader
 */
@Getter
public class JacksonShopManager implements ShopManager {

    private final ShopDataDeserializer shopDataDeserializer = new ShopDataDeserializer();

    private final ShopCreator shopCreator = new ShopCreator();

    public JacksonShopManager() {
        JacksonDataLoader jacksonDataLoader = (JacksonDataLoader) Zombies.getInstance().getDataLoader();
        jacksonDataLoader.addDeserializer(ShopData.class, shopDataDeserializer);
        jacksonDataLoader.addDeserializer(TeamMachineTask.class, new TeamMachineTaskDeserializer());

        addShop(ShopType.ARMOR_SHOP.name(), ArmorShopData.class, ArmorShop::new);
        addShop(ShopType.GUN_SHOP.name(), GunShopData.class, GunShop::new);
        addShop(ShopType.LUCKY_CHEST.name(), LuckyChestData.class, LuckyChest::new);
        addShop(ShopType.PERK_MACHINE.name(), PerkMachineData.class, PerkMachine::new);
        addShop(ShopType.POWER_SWITCH.name(), PowerSwitchData.class, PowerSwitch::new);
        addShop(ShopType.TEAM_MACHINE.name(), TeamMachineData.class, TeamMachine::new);
        addShop(ShopType.ULTIMATE_MACHINE.name(), UltimateMachineData.class, UltimateMachine::new);
    }

    @Override
    public <D extends ShopData> void addShop(String shopType, Class<D> dataClass,
                                             ShopCreator.ShopMapping<D> shopMapping) {
        shopDataDeserializer.getShopDataClassMappings().put(shopType, dataClass);
        shopCreator.getShopMappings().put(shopType, shopMapping);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends ShopData> Shop<D> createShop(ZombiesArena zombiesArena, D shopData) {
        ShopCreator.ShopMapping<D> shopMapping
                = (ShopCreator.ShopMapping<D>) shopCreator.getShopMappings().get(shopData.getType());

        return shopMapping.createShop(zombiesArena, shopData);
    }

}
