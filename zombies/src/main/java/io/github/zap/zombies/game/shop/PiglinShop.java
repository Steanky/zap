package io.github.zap.zombies.game.shop;

import io.github.zap.arenaapi.BukkitTaskManager;
import io.github.zap.arenaapi.DisposableBukkitRunnable;
import io.github.zap.arenaapi.game.arena.event.ManagedPlayerArgs;
import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.arenaapi.hotbar.HotbarObject;
import io.github.zap.arenaapi.hotbar.HotbarObjectGroup;
import io.github.zap.arenaapi.util.TimeUtil;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.data.equipment.EquipmentCreator;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.data.equipment.EquipmentDataManager;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.shop.PiglinShopData;
import io.github.zap.zombies.game.equipment.Equipment;
import io.github.zap.zombies.game.equipment.gun.Gun;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PiglinShop extends Shop<@NotNull PiglinShopData> {

    private final static @NotNull Random RANDOM = new Random();

    private final @NotNull EntityPiglin dream;

    private final @NotNull Hologram hologram;

    private final @NotNull EquipmentCreator equipmentCreator;

    private final @NotNull List<@NotNull EquipmentData<@NotNull ?>> equipments = new ArrayList<>();

    private final BukkitTaskManager taskManager;

    private EquipmentData<@NotNull ?> equipmentData;

    private Sitter sitter;

    private boolean init = false;

    private boolean active = false;

    private boolean doneThinking = false;

    private Player roller;

    public PiglinShop(@NotNull World world, @NotNull ShopEventManager eventManager, @NotNull PiglinShopData shopData,
                      @NotNull MapData map, @NotNull EquipmentDataManager equipmentDataManager,
                      @NotNull EquipmentCreator equipmentCreator, @NotNull BukkitTaskManager taskManager) {
        super(world, eventManager, shopData);

        this.dream = new EntityPiglin(EntityTypes.PIGLIN, ((CraftWorld) world).getHandle()) {
            @Nullable
            @Override
            public GroupDataEntity prepare(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity, @Nullable NBTTagCompound nbttagcompound) {
                return null;
            }

            @Override
            protected void a(DifficultyDamageScaler difficultydamagescaler) {

            }

            @Override
            public EnumInteractionResult b(EntityHuman entityhuman, EnumHand enumhand) {
                return EnumInteractionResult.PASS;
            }

            @Override
            protected void mobTick() {

            }

            @Override
            public boolean damageEntity(DamageSource damagesource, float f) {
                return false;
            }

            @Override
            protected void b(EntityItem entityitem) {

            }

            @Override
            public boolean isCollidable() {
                return false;
            }
        };
        this.dream.setInvulnerable(true);
        this.dream.setPersistent();
        this.dream.setNoAI(true);

        this.hologram = new Hologram(shopData.getPiglinLocation().add(new Vector(0, 1, 0)).toLocation(world));
        for (String equipmentName : shopData.getEquipments()) {
            EquipmentData<@NotNull ?> equipmentData = equipmentDataManager.getEquipmentData(map.getName(),
                    equipmentName);
            if (equipmentData != null) {
                equipments.add(equipmentData);
            }
        }

        this.equipmentCreator = equipmentCreator;
        this.taskManager = taskManager;
    }

    public void setActive(boolean active) {
        if (active) {
            if (!this.active) {
                this.active = true;
                display();
            }
        } else if (this.active) {
            hologram.destroy();
            this.active = false;
            display();
        }
    }

    @Override
    public void display() {
        if (active && roller == null) {
            while (hologram.getHologramLines().size() < 2) {
                hologram.addLine("");
            }

            hologram.updateLineForEveryone(0, String.format("%s%sDream Piglin", ChatColor.GOLD, ChatColor.BOLD));
            hologram.updateLineForEveryone(1,
                    getShopData().isRequiresPower() && !isPowered()
                            ? String.format("%sRequires Power!", ChatColor.GRAY)
                            : String.format("%s%s%d Gold", ChatColor.YELLOW, ChatColor.BOLD, getShopData().getCost()));

        }
        if (!init) {
            ((CraftWorld) getWorld()).addEntity(dream, CreatureSpawnEvent.SpawnReason.CUSTOM);
            dream.setPositionRotation(getShopData().getPiglinLocation().getX(), getShopData().getPiglinLocation().getY(), getShopData().getPiglinLocation().getZ(), getShopData().getDirection(), 0.0F);
            dream.setHeadRotation(getShopData().getDirection());
            init = true;
        }

        super.display();
    }

    @Override
    protected void displayToPlayer(Player player) {
        super.displayToPlayer(player);
        hologram.renderToPlayer(player);
    }

    @Override
    public boolean interact(@NotNull ManagedPlayerArgs<@NotNull ZombiesPlayer, ? extends @NotNull PlayerEvent> args) {
        if (args.event() instanceof PlayerInteractEntityEvent event && event.getRightClicked().getUniqueId().equals(dream.getUniqueID())) {
            ZombiesPlayer player = args.player();

            if (getShopData().isRequiresPower() && !isPowered()) {
                player.getPlayer().sendMessage(Component.text("I need some power to trade!",
                        NamedTextColor.RED));
            } else if (!active) {
                String notActive = "Shop's not open right now.";
                String piglinRoom = getArena().getPiglinRoom();
                if (piglinRoom != null) {
                    notActive += " Go to " + piglinRoom + ", will ya?";
                }

                player.getPlayer().sendMessage(Component.text(notActive, NamedTextColor.RED));
            } else if (roller != null) {
                if (player.getPlayer().equals(roller)) {
                    if (doneThinking) {
                        if (attemptToClaim(player)) {
                            return true;
                        }
                    } else {
                        player.getPlayer().sendMessage(Component.text("Hey, let me think what to give you!",
                                NamedTextColor.RED));
                    }
                } else {
                    player.getPlayer().sendMessage(Component.text("I'm trading with someone else!",
                            NamedTextColor.RED));
                }
            } else {
                int cost = getShopData().getCost();
                if (player.getCoins() < cost) {
                    player.getPlayer().sendMessage(Component.text("You don't have enough coins to trade " +
                            "with me!", NamedTextColor.RED));
                } else {
                    player.subtractCoins(cost);

                    hologram.destroy();
                    roller = player.getPlayer();
                    doneThinking = false;

                    dream.setSlot(EnumItemSlot.OFFHAND, new ItemStack(Items.GOLD_INGOT));
                    taskManager.runTaskLater(120L, () -> {
                        equipmentData = equipments.get(RANDOM.nextInt(equipments.size()));

                        dream.setSlot(EnumItemSlot.OFFHAND, ItemStack.NULL_ITEM);
                        dream.setSlot(EnumItemSlot.MAINHAND,
                                CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(equipmentData
                                        .getMaterial())));

                        taskManager.runTaskTimer(0L, 2L, sitter = new Sitter(player));
                    });

                    return true;
                }
            }

            player.getPlayer().playSound(Sound.sound(Key.key("minecraft:entity.piglin.angry"), Sound.Source.MASTER,
                    1.0F, 1.0F));
            return true;
        }

        return false;
    }

    @Override
    public String getShopType() {
        return ShopType.PIGLIN_SHOP.name();
    }

    /**
     * Attempts to claim the piglin weapon
     *
     * @param player The claimant player
     * @return Whether claim was successful
     */
    private boolean attemptToClaim(@NotNull ZombiesPlayer player) {
        HotbarObjectGroup equipmentObjectGroup = player.getHotbarManager()
                .getHotbarObjectGroup(equipmentData.getEquipmentObjectGroupType());

        if (equipmentObjectGroup != null) {
            if (attemptToRefill(equipmentObjectGroup, equipmentData)) {
                return true;
            }

            Integer nextSlot = equipmentObjectGroup.getNextEmptySlot();
            if (nextSlot == null) {
                int heldSlot = player.getPlayer().getInventory().getHeldItemSlot();
                if (equipmentObjectGroup.getHotbarObjectMap().containsKey(heldSlot)) {
                    nextSlot = heldSlot;
                }
            }
            if (nextSlot != null) {
                Equipment<@NotNull ?, @NotNull ?> equipment = equipmentCreator.createEquipment(player, nextSlot,
                        equipmentData);
                if (equipment != null) {
                    equipmentObjectGroup.setHotbarObject(nextSlot, equipment);
                    player.getPlayer().playSound(Sound.sound(Key.key("minecraft:block.note_block.pling"),
                            Sound.Source.MASTER, 1.0F, 2.0F));
                }
                else {
                    Zombies.warning("Failed to create equipment with name " + equipmentData.getName() + "!");
                    player.getPlayer().sendMessage(Component.text("This shop was not set up correctly",
                            NamedTextColor.RED));
                }

                sitter.destroy();

                return true;
            } else {
                player.getPlayer().sendMessage(Component.text("Choose a slot to receive the item in!",
                        NamedTextColor.RED));
            }
        } else {
            player.getPlayer().sendMessage(Component.text("You can't claim this weapon!", NamedTextColor.RED));
            sitter.destroy();
        }

        return false;
    }

    /**
     * Attempts to refill a gun upon claim
     *
     * @param hotbarObjectGroup The object group in which the gun may reside
     * @param equipmentData     The equipment data of the gun
     * @return Whether refill was successful
     */
    private boolean attemptToRefill(@NotNull HotbarObjectGroup hotbarObjectGroup,
                                    @NotNull EquipmentData<?> equipmentData) {
        for (HotbarObject hotbarObject : hotbarObjectGroup.getHotbarObjectMap().values()) {
            if (hotbarObject instanceof Gun<@NotNull ?, @NotNull ?> gun &&
                    gun.getEquipmentData().getName().equals(equipmentData.getName())) {
                gun.refill();

                sitter.destroy();

                hotbarObjectGroup.tryGetPlayer().playSound(Sound.sound(Key.key("minecraft:block.note_block.pling"),
                        Sound.Source.MASTER, 1.0F, 2.0F));

                return true;
            }
        }

        return false;
    }

    private class Sitter extends DisposableBukkitRunnable {

        private final ZombiesPlayer player;

        private final Hologram endHologram = new Hologram(getShopData().getPiglinLocation()
                .add(new Vector(0, 1, 0)).toLocation(getWorld()));

        private long sittingTime = getShopData().getSittingTime();

        public Sitter(ZombiesPlayer player) {
            this.player = player;

            doneThinking = true;
            endHologram.addLine(ChatColor.RED + "Right Click to Claim!");
            endHologram.addLine(ChatColor.RED + equipmentData.getDisplayName());
            endHologram.addLine(ChatColor.YELLOW + equipmentData.getDisplayName());

            if (roller.isOnline()) {
                roller.sendMessage(Component.text("You got a ", NamedTextColor.RED)
                        .append(Component.text(equipmentData.getDisplayName(), NamedTextColor.YELLOW))
                        .append(Component.text("!", NamedTextColor.RED)));
            }
        }

        @Override
        public void run() {
            if (sittingTime > 0) {
                String timeRemaining = TimeUtil.convertTicksToSecondsString(sittingTime);
                endHologram.updateLineForEveryone(1, String.format("%s%s", ChatColor.RED, timeRemaining));

                sittingTime -= 2;
            } else {
                destroy();
            }
        }

        public void destroy() {
            endHologram.destroy();
            equipmentData = null;
            roller = null;
            dream.setSlot(EnumItemSlot.MAINHAND, ItemStack.NULL_ITEM);

            display();
            onPurchaseSuccess(player);

            cancel();
        }
    }

}
