package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.zombies.game.data.shop.UltimateMachineData;
import io.github.zap.zombies.game.equipment.UpgradeableEquipment;
import io.github.zap.zombies.game.equipment.UpgradeableEquipmentObjectGroup;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Machine for upgrading designated upgradeable equipment
 */
public class UltimateMachine extends BlockShop<@NotNull UltimateMachineData> {

    public UltimateMachine(@NotNull World world, @NotNull ShopEventManager eventManager,
                           @NotNull UltimateMachineData shopData) {
        super(world, eventManager, shopData);
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
    public boolean interact(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, ? extends @NotNull PlayerEvent> args) {
        if (super.interact(args)) {
            ZombiesPlayer player = args.player();

            if (!getShopData().isRequiresPower() || isPowered()) {
                int cost = getShopData().getCost();

                if (player.getCoins() < cost) {
                    player.getPlayer().sendMessage(Component.text("You cannot afford this item!",
                            NamedTextColor.RED));
                } else {
                    HotbarObject hotbarObject = player.getHotbarManager().getSelectedObject();
                    if (hotbarObject instanceof UpgradeableEquipment<?, ?> upgradeableEquipment
                            && player.getHotbarManager().getSelectedHotbarObjectGroup()
                            instanceof UpgradeableEquipmentObjectGroup upgradeableEquipmentObjectGroup
                            && upgradeableEquipmentObjectGroup.isUltimateable()) {
                        attemptToUltimate(upgradeableEquipment, player, cost);
                        return true;
                    } else {
                        player.getPlayer().sendMessage(Component.text("Choose a slot to receive the upgrade for!",
                                NamedTextColor.RED));
                    }
                }
            } else {
                player.getPlayer().sendMessage(Component.text("The power is not active yet!", NamedTextColor.RED));
            }

            player.getPlayer().playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"),
                    Sound.Source.MASTER, 1.0F, 0.5F));
            return true;
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.ULTIMATE_MACHINE.name();
    }

    private void attemptToUltimate(@NotNull UpgradeableEquipment<@NotNull ?, @NotNull ?> equipment, @NotNull ZombiesPlayer player,
                                      int cost) {
        if (equipment.getLevel() < equipment.getEquipmentData().getLevels().size() - 1) {
            equipment.upgrade();

            player.getPlayer().playSound(Sound.sound(Key.key("minecraft:entity.player.levelup"),
                    Sound.Source.MASTER, 1.0F, 1.0F));

            player.subtractCoins(cost);
            onPurchaseSuccess(player);
            return;
        }

        player.getPlayer().sendMessage(Component.text("You have already maxed out this item!",
                NamedTextColor.RED));
    }

}
