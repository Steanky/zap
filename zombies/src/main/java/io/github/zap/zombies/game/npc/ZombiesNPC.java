package io.github.zap.zombies.game.npc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.game.Joinable;
import io.github.zap.arenaapi.game.SimpleJoinable;
import io.github.zap.arenaapi.game.arena.JoinInformation;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.map.MapData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * NPC that allows you to join a zombies game
 */
public class ZombiesNPC implements Listener {

    private final static String NAME = "Play Zombies";

    private final static Map<Integer, ZombiesNPC> NPC_MAP = new HashMap<>();

    private final static PacketAdapter packetAdapter
            = new PacketAdapter(Zombies.getInstance(), PacketType.Play.Client.USE_ENTITY) {

        @Override
        public void onPacketReceiving(PacketEvent event) {
            if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
                int id = event.getPacket().getIntegers().read(0);

                ZombiesNPC zombiesNPC = NPC_MAP.get(id);
                if (zombiesNPC != null) {
                    Player player = event.getPlayer();

                    Bukkit.getScheduler().runTask(Zombies.getInstance(), () -> {
                        player.openInventory(zombiesNPC.inventory);
                    });
                }
            }
        }

    };

    private final Location location;

    private final int id;

    private final Inventory inventory;

    private final Map<Integer, String> mapNameMap = new HashMap<>();

    private final PacketContainer playerAddPacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

    private final PacketContainer playerRemovePacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

    private final PacketContainer spawnPlayerPacket = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);

    private final PacketContainer playerMetadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

    private final PacketContainer playerHeadRotationPacket
            = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);

    private final PacketContainer playerLookPacket = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);

    public ZombiesNPC(World world, ZombiesNPCData data) {
        this.location = data.location.toLocation(world);
        this.id = Zombies.getInstance().getNmsProxy().nextEntityId();

        // potentially init packet listener
        NPC_MAP.put(id, this);
        if (NPC_MAP.size() == 1) {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.addPacketListener(packetAdapter);
        }

        // init player info data
        UUID uniqueId = UUID.randomUUID();
        WrappedGameProfile wrappedGameProfile = new WrappedGameProfile(uniqueId, NAME);
        WrappedSignedProperty texture = data.texture;
        if (texture != null) {
            wrappedGameProfile.getProperties().put("textures", texture);
        }

        PlayerInfoData playerInfoData = new PlayerInfoData(
                wrappedGameProfile,
                0,
                EnumWrappers.NativeGameMode.NOT_SET,
                WrappedChatComponent.fromText(NAME)
        );
        List<PlayerInfoData> playerInfoDataList = Collections.singletonList(playerInfoData);


        // init player add packet
        playerAddPacket.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        playerAddPacket.getPlayerInfoDataLists().write(0, playerInfoDataList);


        // init player remove packet
        playerRemovePacket.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        playerRemovePacket.getPlayerInfoDataLists().write(0, playerInfoDataList);


        // init spawn player packet
        spawnPlayerPacket.getIntegers().write(0, id);
        spawnPlayerPacket.getUUIDs().write(0, uniqueId);
        spawnPlayerPacket.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());


        // init player metadata packet
        playerMetadataPacket.getIntegers().write(0, id);

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer overlaySerializer = WrappedDataWatcher.Registry.get(Byte.class);
        WrappedDataWatcher.WrappedDataWatcherObject overlay
                = new WrappedDataWatcher.WrappedDataWatcherObject(16, overlaySerializer);

        wrappedDataWatcher.setObject(overlay, (byte) 0x7F);

        playerMetadataPacket.getWatchableCollectionModifier()
                .write(0, wrappedDataWatcher.getWatchableObjects());


        // init player head rotation packet
        playerHeadRotationPacket.getIntegers().write(0, id);
        playerHeadRotationPacket.getBytes()
                .write(0, (byte) (data.direction * 256.0F / 360.0F));


        // init player head look packet
        playerLookPacket.getIntegers().write(0, id);
        playerLookPacket.getBytes()
                .write(0, (byte) (data.direction * 256.0F / 360.0F))
                .write(1, (byte) 0F);
        playerLookPacket.getBooleans().write(0, true);

        // init GUI inventory
        List<MapData> mapDataList = Zombies.getInstance().getArenaManager().getMaps();
        int num = mapDataList.size();

        if (num > 0) {
            int width = (int) Math.ceil(Math.sqrt(num));
            int height = (int) Math.ceil((double) num / width);
            int remainderLine = Math.min(6, height) / 2;
            // this is the first line offset
            int offset = (height <= 4) ? 1 : 0;
            // If the height go higher than 6 we need to change our calculation
            if (height > 6) {
                width = (int) Math.ceil((double) num / 6);
            }
            int finalLine = num % width;
            if (finalLine == 0) {
                finalLine = width;
            }

            int guiSize = 9 * Math.min(6, height + 2);
            inventory = Bukkit.createInventory(null, guiSize, Component.text("Team Machine"));

            int index = 0;

            for (int h = 0; h < height; h++) {
                int lineCount = (h == remainderLine) ? finalLine : width;
                for (int w = 0; w < lineCount && index < num; w++) {
                    int slot = (18 * w + 9) / (2 * lineCount);
                    int pos = (h + offset) * 9 + slot;

                    MapData mapData = mapDataList.get(index);

                    ItemStack mapDataItemStackRepresentation = new ItemStack(mapData.getItemStackMaterial());
                    mapDataItemStackRepresentation.setLore(mapData.getItemStackLore());

                    ItemMeta itemMeta = mapDataItemStackRepresentation.getItemMeta();
                    itemMeta.displayName(Component.text(mapData.getItemStackDisplayName()));

                    mapDataItemStackRepresentation.setItemMeta(itemMeta);

                    inventory.setItem(pos, mapDataItemStackRepresentation);

                    mapNameMap.put(pos, mapData.getName());
                    index++;
                }
            }
        } else {
            inventory = Bukkit.createInventory(null, 9, Component.text("Play Zombies"));
        }

        Bukkit.getServer().getPluginManager().registerEvents(this, Zombies.getInstance());
    }

    /**
     * Displays the NPC to a player
     * @param player Thep layer to display the NPC to
     */
    public void displayToPlayer(Player player) {
        Zombies zombies = Zombies.getInstance();
        ArenaApi arenaApi = ArenaApi.getInstance();
        arenaApi.sendPacketToPlayer(zombies, player, playerAddPacket);
        arenaApi.sendPacketToPlayer(zombies, player, spawnPlayerPacket);
        arenaApi.sendPacketToPlayer(zombies, player, playerMetadataPacket);
        arenaApi.sendPacketToPlayer(zombies, player, playerHeadRotationPacket);
        arenaApi.sendPacketToPlayer(zombies, player, playerLookPacket);
        arenaApi.sendPacketToPlayer(zombies, player, playerRemovePacket);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld().equals(location.getWorld())) {
            displayToPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld().equals(location.getWorld())) {
            displayToPlayer(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory != null && clickedInventory.equals(inventory) && humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            String mapData = mapNameMap.get(event.getSlot());

            if (mapData != null) {
                player.closeInventory();

                ArenaApi arenaApi = Zombies.getInstance().getArenaApi();
                Joinable joinable = new SimpleJoinable(Collections.singletonList(player));

                JoinInformation testInformation = new JoinInformation(
                        joinable,
                        Zombies.getInstance().getArenaManager().getGameName(),
                        mapData,
                        null,
                        null
                );

                arenaApi.handleJoin(testInformation, (pair) -> {
                    if (!pair.getLeft()) {
                        player.sendMessage(pair.getRight());
                    }
                });
            }
        }
    }

    /**
     * Destroys the NPC
     */
    public void destroy() {
        // Remove NPC
        PacketContainer killPacketContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        killPacketContainer.getIntegerArrays().write(0, new int[] { id });

        Zombies zombies = Zombies.getInstance();
        ArenaApi arenaApi = ArenaApi.getInstance();
        for (Player player : location.getWorld().getPlayers()) {
            arenaApi.sendPacketToPlayer(zombies, player, killPacketContainer);
        }


        // Stop tracking npc and disable packet listener if necessary
        NPC_MAP.remove(id);
        if (NPC_MAP.size() == 0) {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.removePacketListener(packetAdapter);
        }


        HandlerList.unregisterAll(this);
    }

    @RequiredArgsConstructor
    @Getter
    public static class ZombiesNPCData implements ConfigurationSerializable {

        private final Vector location;

        private final float direction;

        private final WrappedSignedProperty texture;

        @Override
        public @NotNull Map<String, Object> serialize() {
            Map<String, Object> serialized = new HashMap<>();
            serialized.put("location", location);
            serialized.put("direction", direction);

            if (texture != null) {
                serialized.put("textureValue", texture.getValue());
                serialized.put("signature", texture.getSignature());
            }

            return serialized;
        }

        @SuppressWarnings("unused")
        public static ZombiesNPCData deserialize(Map<String, Object> data) {
            Vector location = (Vector) data.get("location");
            float direction = (float) (double) data.get("direction");

            String textureValue = (String) data.get("textureValue");
            String signature = (String) data.get("signature");

            WrappedSignedProperty texture;
            if (textureValue != null && signature != null) {
                texture = WrappedSignedProperty.fromValues("textures", textureValue, signature);
            } else {
                texture = null;
            }

            return new ZombiesNPCData(location, direction, texture);
        }

    }

}
