package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.map.shop.UltimateMachineData;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import io.github.zap.zombies.game.equipment.UpgradeableEquipmentObjectGroup;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Machine for upgrading designated upgradeable equipment
 */
public class UltimateMachine extends BlockShop<UltimateMachineData> {

    public UltimateMachine(ZombiesArena zombiesArena, UltimateMachineData shopData) {
        super(zombiesArena, shopData);
    }

    @Override
    public void display() {
        Hologram hologram = getHologram();
        while (hologram.getHologramLines().size() < 2) {
            hologram.addLine("");
        }
        super.display();
    }

    @Override
    protected void displayToPlayer(Player player) {
        Hologram hologram = getHologram();

        hologram.updateLineForPlayer(player, 0, ChatColor.GOLD + "Ultimate Machine");

        hologram.updateLineForPlayer(player, 1,
                getShopData().isRequiresPower() && !isPowered()
                        ? ChatColor.GRAY + "Requires Power!"
                        : String.format("%s%d Gold", ChatColor.GOLD, getShopData().getCost())
        );
    }

    @Override
    public boolean purchase(ZombiesArena.ProxyArgs<? extends Event> args) {
        if (super.purchase(args)) {
            ZombiesPlayer player = args.getManagedPlayer();

            if (player != null) {
                Player bukkitPlayer = player.getPlayer();

                if (bukkitPlayer != null) {
                    UltimateMachineData shopData = getShopData();
                    if (!shopData.isRequiresPower() || isPowered()) {
                        int cost = shopData.getCost();

                        if (player.getCoins() < cost) {
                            bukkitPlayer.sendMessage(ChatColor.RED + "You cannot afford this item!");
                        } else {
                            HotbarObject hotbarObject = player.getHotbarManager().getSelectedObject();
                            if (hotbarObject instanceof UpgradeableEquipment<?, ?> upgradeableEquipment
                                    && player.getHotbarManager().getSelectedHotbarObjectGroup()
                                    instanceof UpgradeableEquipmentObjectGroup upgradeableEquipmentObjectGroup
                                    && upgradeableEquipmentObjectGroup.isUltimateable()) {
                                return attemptToUltimate(upgradeableEquipment, player, cost);
                            } else {
                                bukkitPlayer.sendMessage(Component
                                        .text("Choose a slot to receive the upgrade for!", NamedTextColor.RED));
                            }
                        }
                    } else {
                        bukkitPlayer.sendMessage(Component.text("The power is not active yet!",
                                NamedTextColor.RED));
                    }

                    bukkitPlayer.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"),
                            Sound.Source.MASTER, 1.0F, 0.5F));
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.ULTIMATE_MACHINE.name();
    }

    private boolean attemptToUltimate(@NotNull UpgradeableEquipment<?, ?> equipment, @NotNull ZombiesPlayer player,
                                      int cost) {
        Player bukkitPlayer = player.getPlayer();

        if (bukkitPlayer != null) {
            if (equipment.getLevel() + 1 < equipment.getEquipmentData().getLevels().size()) {
                equipment.upgrade();

                bukkitPlayer.playSound(Sound.sound(Key.key("minecraft:entity.player.levelup"),
                        Sound.Source.MASTER, 1.0F, 1.0F));

                player.subtractCoins(cost);
                onPurchaseSuccess(player);

                return true;
            }

            bukkitPlayer.sendMessage(Component.text("You have already maxed out this item!",
                    NamedTextColor.RED));
        }

        return false;
    }

}
