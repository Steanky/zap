package io.github.zap.zombies.game.shop;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hologram.HologramLine;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.ZombiesPlayer;
import io.github.zap.zombies.game.data.map.shop.ArmorShopData;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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

    private static ArmorShopData modifyArmorStandLocation(ArmorShopData shopData) {
        shopData.getRootLocation().add(new Vector(0, 1.5, 0));
        return shopData;
    }

    public ArmorShop(ZombiesArena zombiesArena, ArmorShopData shopData) {
        super(zombiesArena, modifyArmorStandLocation(shopData));
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        ArmorStand armorStand = getArmorStand();
        Location armorStandLocation = getArmorStand().getLocation();
        armorStandLocation.setYaw(getShopData().getArmorStandDirection());
        armorStand.teleport(armorStandLocation);
        armorStand.setSmall(true);

        getZombiesArena().getChunkLoadHandler().addConsumer(armorStandLocation, this::displayTo);
    }

    @Override
    protected void registerArenaEvents() {
        super.registerArenaEvents();
        getZombiesArena().getShopEvent(getShopType()).registerHandler(args -> display());
    }

    @Override
    public void display() {
        Hologram hologram = getHologram();

        List<HologramLine<?>> lines = hologram.getHologramLines();
        while (lines.size() < 2) {
            hologram.addLine("");
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
        String secondHologramLine;
        if (armorLevel == null) {
            armorLevel = armorLevels.get(armorLevels.size() - 1);
            secondHologramLine = ChatColor.RED + "You have already unlocked this item!";
        } else {
            secondHologramLine = (armorShopData.isRequiresPower() && !isPowered())
                    ? ChatColor.GRAY + "Requires Power!"
                    : String.format("%s%d Gold", ChatColor.GOLD, armorLevel.getCost());
        }

        sendArmorStandUpdatePackets(player, armorLevel);

        hologram.updateLineForPlayer(player, 0, ChatColor.GREEN + armorLevel.getName());
        hologram.updateLineForPlayer(player, 1, secondHologramLine);
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            ZombiesPlayer zombiesPlayer = args.getManagedPlayer();

            if(zombiesPlayer != null) {
                Player bukkitPlayer = zombiesPlayer.getPlayer();

                if(bukkitPlayer != null) {
                    if (!getShopData().isRequiresPower() || isPowered()) {
                        ArmorShopData.ArmorLevel armorLevel = determineArmorLevel(bukkitPlayer);
                        if (armorLevel == null) {
                            bukkitPlayer.sendMessage(ChatColor.RED + "You already have the max level of this armor!");
                        } else {
                            int cost = armorLevel.getCost();

                            if (zombiesPlayer.getCoins() < cost) {
                                bukkitPlayer.sendMessage(ChatColor.RED + "You cannot afford this item!");
                            } else {
                                // Choose the best equipments
                                Material[] materials = armorLevel.getMaterials();
                                //noinspection ConstantConditions
                                ItemStack[] current = bukkitPlayer.getEquipment().getArmorContents();
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

                                bukkitPlayer.playSound(Sound.sound(
                                        Key.key("block.note_block.pling"),
                                        Sound.Source.MASTER,
                                        1.0F,
                                        2.0F
                                ));

                                zombiesPlayer.updateEquipment(current);
                                zombiesPlayer.subtractCoins(cost);

                                displayTo(bukkitPlayer);
                                onPurchaseSuccess(zombiesPlayer);
                                return true;
                            }
                        }

                    } else {
                        bukkitPlayer.sendMessage(ChatColor.RED + "The power is not active yet!");
                    }

                    bukkitPlayer.playSound(Sound.sound(
                            Key.key("minecraft:entity.enderman.teleport"),
                            Sound.Source.MASTER,
                            1.0F,
                            0.5F
                    ));
                }
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
        //noinspection ConstantConditions
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
        //noinspection ConstantConditions
        ItemStack[] equipment = player.getEquipment().getArmorContents();
        Material[] materials = armorLevel.getMaterials();

        int armorStandId = getArmorStand().getEntityId();

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentSlotStackPairList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Material material = materials[i];
            ItemStack playerItemStack = equipment[i];
            ItemStack itemStack = playerItemStack == null ? null : playerItemStack.clone();

            if (material != null) {
                if (itemStack != null) {
                    itemStack.setType(material);
                } else {
                    itemStack = new ItemStack(material);
                }
            }

            equipmentSlotStackPairList.add(new Pair<>(ITEM_SLOT_MAP.get(i + 2), itemStack));
        }

        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packetContainer.getIntegers().write(0, armorStandId);
        packetContainer.getSlotStackPairLists().write(0, equipmentSlotStackPairList);

        try {
            protocolManager.sendServerPacket(player, packetContainer);
        } catch (InvocationTargetException exception) {
            Zombies.warning(
                    String.format("Error creating armor shop equipment packets for entity id %d", armorStandId)
            );
        }
    }

}
