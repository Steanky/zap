package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.serialize.FieldTypeDeserializer;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.tmtask.*;
import io.github.zap.zombies.game.shop.*;
import lombok.Getter;

/**
 * Class for storing and managing shop data using Jackson's data loader
 */
@Getter
public class JacksonShopManager implements ShopManager {

    private final FieldTypeDeserializer<ShopData> shopDataDeserializer
            = new FieldTypeDeserializer<>("type");

    private final ShopCreator shopCreator = new ShopCreator();

    private final FieldTypeDeserializer<TeamMachineTask> teamMachineTaskFieldTypeDeserializer
            = new FieldTypeDeserializer<>("type");

    public JacksonShopManager() {
        ArenaApi arenaApi = ArenaApi.getInstance();

        arenaApi.addDeserializer(ShopData.class, shopDataDeserializer);
        addShop(ShopType.ARMOR_SHOP, ArmorShopData.class, ArmorShop::new);
        addShop(ShopType.DOOR, DoorData.class, Door::new);
        addShop(ShopType.GUN_SHOP, GunShopData.class, GunShop::new);
        addShop(ShopType.LUCKY_CHEST, LuckyChestData.class, LuckyChest::new);
        addShop(ShopType.PERK_MACHINE, PerkMachineData.class, PerkMachine::new);
        addShop(ShopType.POWER_SWITCH, PowerSwitchData.class, PowerSwitch::new);
        addShop(ShopType.TEAM_MACHINE, TeamMachineData.class, TeamMachine::new);
        addShop(ShopType.ULTIMATE_MACHINE, UltimateMachineData.class, UltimateMachine::new);

        arenaApi.addDeserializer(TeamMachineTask.class, teamMachineTaskFieldTypeDeserializer);
        addTeamMachineTask(TeamMachineTaskType.AMMO_SUPPLY.name(), AmmoSupply.class);
        addTeamMachineTask(TeamMachineTaskType.FULL_REVIVE.name(), FullRevive.class);
        addTeamMachineTask(TeamMachineTaskType.DRAGON_WRATH.name(), DragonWrath.class);
    }

    @Override
    public <D extends ShopData> void addShop(ShopType shopType, Class<D> dataClass,
                                             ShopCreator.ShopMapping<D> shopMapping) {
        shopDataDeserializer.getMappings().put(String.valueOf(shopType), dataClass);
        shopCreator.getShopMappings().put(shopType, shopMapping);
    }

    public <D extends TeamMachineTask> void addTeamMachineTask(String type, Class<D> dataClass) {
        teamMachineTaskFieldTypeDeserializer.getMappings().put(type, dataClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends ShopData> Shop<D> createShop(ZombiesArena zombiesArena, D shopData) {
        ShopCreator.ShopMapping<D> shopMapping
                = (ShopCreator.ShopMapping<D>) shopCreator.getShopMappings().get(shopData.getType());

        return shopMapping.createShop(zombiesArena, shopData);
    }

}
