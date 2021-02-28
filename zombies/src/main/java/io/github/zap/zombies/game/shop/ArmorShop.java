package io.github.zap.zombies.game.shop;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hologram.HologramLine;
import io.github.zap.zombies.MessageKey;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.ArmorShopData;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shop for purchasing pieces of armor at a time
 */
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
    protected void registerArenaEvents() {
        super.registerArenaEvents();
        getZombiesArena().getShopEvents().get(getShopType()).registerHandler(args -> display());
    }

    @Override
    public void display() {
        Hologram hologram = getHologram();

        List<HologramLine<?>> lines = hologram.getHologramLines();
        while (lines.size() < 2) {
            hologram.addLine(MessageKey.PLACEHOLDER.getKey());
        }

        super.display();
    }

    @Override
    protected void displayTo(Player player) {
        Hologram hologram = getHologram();
        ArmorShopData armorShopData = getShopData();

        List<ArmorShopData.ArmorLevel> armorLevels = armorShopData.getArmorLevels();
        ArmorShopData.ArmorLevel armorLevel = determineArmorLevel(player);

        // Display the hologram
        Pair<String, String[]> secondHologramLine;
        if (armorLevel == null) {
            armorLevel = armorLevels.get(armorLevels.size() - 1);
            secondHologramLine = ImmutablePair.of(MessageKey.UNLOCKED.getKey(), new String[]{});
        } else {
            secondHologramLine = (armorShopData.isRequiresPower() && !isPowered())
                    ? ImmutablePair.of(MessageKey.REQUIRES_POWER.getKey(), new String[]{})
                    : ImmutablePair.of(MessageKey.COST.getKey(), new String[]{ String.valueOf(armorLevel.getCost()) });
        }

        sendArmorStandUpdatePackets(player, armorLevel);

        hologram.updateLineForPlayer(player, 0, armorLevel.getName());
        hologram.updateLineForPlayer(player, 1, secondHologramLine);
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            ZombiesPlayer zombiesPlayer = args.getManagedPlayer();
            Player player = zombiesPlayer.getPlayer();

            if (!getShopData().isRequiresPower() || isPowered()) {
                ArmorShopData.ArmorLevel armorLevel = determineArmorLevel(player);
                if (armorLevel == null) {
                    player.sendMessage(ChatColor.RED + "You already have the max level of this armor!");
                } else {
                    int cost = armorLevel.getCost();

                    if (zombiesPlayer.getCoins() < cost) {
                        player.sendMessage(ChatColor.RED + "You cannot afford this item!");
                    } else {
                        // Choose the best equipments
                        Material[] materials = armorLevel.getMaterials();
                        ItemStack[] current = player.getEquipment().getArmorContents();
                        for (int i = 0; i < 4; i++) {
                            Material material = materials[i];
                            ItemStack itemStack = current[i];

                            if (material != null) {
                                if (itemStack != null
                                        && itemStack.getType().getMaxDurability() < material.getMaxDurability()) {
                                    itemStack.setType(material);
                                } else {
                                    current[i] = new ItemStack(material);
                                }
                            }

                        }

                        player.getEquipment().setArmorContents(current);
                        zombiesPlayer.subtractCoins(cost);

                        displayTo(player);
                        onPurchaseSuccess(zombiesPlayer);
                    }
                }

            } else {
                player.sendMessage(ChatColor.RED + "The power is not active yet!");
            }

            return true;
        }
        return false;
    }

    @Override
    public ShopType getShopType() {
        return ShopType.ARMOR_SHOP;
    }

    /**
     * Determines the relevant armor level of the player
     * @param player The player to determine the armor level for
     * @return The armor level of the player, or null if the player's armor is better than the shop can provide
     */
    private ArmorShopData.ArmorLevel determineArmorLevel(Player player) {
        ItemStack[] equipment = player.getEquipment().getArmorContents();
        for (ArmorShopData.ArmorLevel armorLevel : getShopData().getArmorLevels()) {
            Material[] materials = armorLevel.getMaterials();
            for (int i = 0; i < 4; i++) {
                Material material = materials[i];
                ItemStack itemStack = equipment[i];

                // Accept any material that overrides a current item stack's durability or lack thereof
                if (material != null
                        &&
                        (itemStack == null || material.getMaxDurability() > itemStack.getType().getMaxDurability())) {
                    return armorLevel;
                }
            }
        }

        return null;
    }

    /**
     * Sends packets relating to a player's current armor and the armor shop itself
     * @param player The player to send the packets to
     * @param armorLevel The armor level to compare the player's armor against
     */
    private void sendArmorStandUpdatePackets(Player player, ArmorShopData.ArmorLevel armorLevel) {
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
                Zombies.getInstance().getLogger().warning(
                        String.format("Error creating armor shop equipment packets for entity id %d", armorStandId)
                );
            }
        }
    }

}
