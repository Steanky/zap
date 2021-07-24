package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.serialize.FieldTypeDeserializer;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.tmtask.*;
import io.github.zap.zombies.game.shop.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Class for storing and managing shop data using Jackson's data loader
 */
public class JacksonShopDataManager implements ShopDataManager {

    private final FieldTypeDeserializer<ShopData> shopDataDeserializer
            = new FieldTypeDeserializer<>("type");

    private final FieldTypeDeserializer<TeamMachineTask> teamMachineTaskFieldTypeDeserializer
            = new FieldTypeDeserializer<>("type");

    public JacksonShopDataManager() {
        ArenaApi arenaApi = ArenaApi.getInstance();

        arenaApi.addDeserializer(ShopData.class, shopDataDeserializer);
        addShop(ShopType.ARMOR_SHOP.name(), ArmorShopData.class);
        addShop(ShopType.DOOR.name(), DoorData.class);
        addShop(ShopType.GUN_SHOP.name(), GunShopData.class);
        addShop(ShopType.LUCKY_CHEST.name(), LuckyChestData.class);
        addShop(ShopType.PIGLIN_SHOP.name(), PiglinShopData.class);
        addShop(ShopType.PERK_MACHINE.name(), PerkMachineData.class);
        addShop(ShopType.POWER_SWITCH.name(), PowerSwitchData.class);
        addShop(ShopType.TEAM_MACHINE.name(), TeamMachineData.class);
        addShop(ShopType.ULTIMATE_MACHINE.name(), UltimateMachineData.class);

        arenaApi.addDeserializer(TeamMachineTask.class, teamMachineTaskFieldTypeDeserializer);
        addTeamMachineTask(TeamMachineTaskType.AMMO_SUPPLY.name(), AmmoSupply.class);
        addTeamMachineTask(TeamMachineTaskType.FULL_REVIVE.name(), FullRevive.class);
        addTeamMachineTask(TeamMachineTaskType.DRAGON_WRATH.name(), DragonWrath.class);
    }

    @Override
    public <D extends ShopData> void addShop(@NotNull String shopType, @NotNull Class<D> dataClass) {
        shopDataDeserializer.getMappings().put(shopType, dataClass);
    }

    @Override
    public void addTeamMachineTask(@NotNull String type, @NotNull Class<? extends TeamMachineTask> clazz) {
        teamMachineTaskFieldTypeDeserializer.getMappings().put(type, clazz);
    }

}
