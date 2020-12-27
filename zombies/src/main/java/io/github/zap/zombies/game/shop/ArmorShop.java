package io.github.zap.zombies.game.shop;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.ArmorShopData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmorShop extends ArmorStandShop<ArmorShopData> {

    private static final Map<Integer, EnumWrappers.ItemSlot> ITEM_SLOT_MAP = new HashMap<>() {
        {
            put(2, EnumWrappers.ItemSlot.FEET);
            put(3, EnumWrappers.ItemSlot.LEGS);
            put(4, EnumWrappers.ItemSlot.CHEST);
            put(5, EnumWrappers.ItemSlot.HEAD);
        }
    };

    private final ProtocolManager protocolManager;

    public ArmorShop(ZombiesArena zombiesArena, ArmorShopData shopData) {
        super(zombiesArena, shopData);
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        ArmorStand armorStand = getArmorStand();
        armorStand.teleport(getArmorStand().getLocation().clone().add(0, 1.5, 0));
        armorStand.setSmall(true);
    }

    @Override
    public void onOtherShopPurchase(String shopType) {
        super.onOtherShopPurchase(shopType);
        if (shopType.equals(getShopType())) {
            display();
        }
    }

    @Override
    public void displayTo(Player player) {
        ArmorShopData.ArmorLevel armorLevel = determineArmorLevel(player);

        getHologram().setLineFor(player, 1,
                ChatColor.GOLD + ((armorLevel == null) ? "UNLOCKED" : armorLevel.getCost() + " Gold")
                );

        List<ArmorShopData.ArmorLevel> armorLevels = getShopData().getArmorLevels();
        if (armorLevel == null) armorLevel = armorLevels.get(armorLevels.size() - 1);


        ItemStack[] equipment = player.getEquipment().getArmorContents();
        Material[] materials = armorLevel.getMaterials();
        int armorStandId = getArmorStand().getEntityId();
        for (int i = 0; i < 4; i++) {
            Material material = materials[i];
            ItemStack itemStack = equipment[i];
            if (material != null) {
                if (itemStack != null) {
                    itemStack.setType(material);
                } else {
                    itemStack = new ItemStack(material);
                }
            }
            PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
            packetContainer.getIntegers().write(0, armorStandId);
            packetContainer.getItemSlots().write(0, ITEM_SLOT_MAP.get(i + 2));
            packetContainer.getItemModifier().write(0, itemStack);

            try {
                protocolManager.sendServerPacket(player, packetContainer);
            } catch (InvocationTargetException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public boolean purchase(ZombiesPlayer zombiesPlayer) {
        Player player = zombiesPlayer.getPlayer();
        ArmorShopData.ArmorLevel armorLevel = determineArmorLevel(player);
        if (armorLevel == null) {
            // TODO: ya done now
            return false;
        } else {
            Material[] materials = armorLevel.getMaterials();
            ItemStack[] current = player.getEquipment().getArmorContents();
            for (int i = 0; i < 4; i++) {
                Material material = materials[i];
                ItemStack itemStack = current[i];

                if (material != null) {
                    if (itemStack != null && itemStack.getType().getMaxDurability() < material.getMaxDurability()) {
                        itemStack.setType(material);
                    } else {
                        current[i] = new ItemStack(material);
                    }
                }

            }
            player.getEquipment().setArmorContents(current);
            displayTo(player);

            return true;
        }
    }

    @Override
    public String getShopType() {
        return ShopType.ARMOR_SHOP.name();
    }

    private ArmorShopData.ArmorLevel determineArmorLevel(Player player) {
        ItemStack[] equipment = player.getEquipment().getArmorContents();
        for (ArmorShopData.ArmorLevel armorLevel : getShopData().getArmorLevels()) {
            Material[] materials = armorLevel.getMaterials();
            for (int i = 0; i < 4; i++) {
                Material material = materials[i];
                ItemStack itemStack = equipment[i];
                if (material != null) {
                    if (itemStack != null) {
                        int currentDurability = itemStack.getType().getMaxDurability();
                        int armorDurability = material.getMaxDurability();

                        if (armorDurability > currentDurability) {
                            return armorLevel;
                        }
                    } else {
                        return armorLevel;
                    }
                }
            }
        }

        return null;
    }

}
