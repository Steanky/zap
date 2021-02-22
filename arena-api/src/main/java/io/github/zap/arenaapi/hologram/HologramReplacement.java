package io.github.zap.arenaapi.hologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import io.github.zap.arenaapi.ArenaApi;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HologramReplacement {

    private static final double DEFAULT_LINE_SPACE = 0.25;

    private static final Set<Integer> TEXT_LINE_SET = new HashSet<>();

    private final ArenaApi arenaApi;

    @Getter
    private final List<HologramLine<?>> hologramLines = new ArrayList<>();

    private final double lineSpace;

    private final Location rootLocation;

    static {
        ArenaApi arenaApi = ArenaApi.getInstance();
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(arenaApi, ListenerPriority.NORMAL, PacketType.Play.Client
                .USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
                    PacketContainer packetContainer = event.getPacket();

                    if (packetContainer.getEntityUseActions().read(0) == EnumWrappers.EntityUseAction
                            .INTERACT_AT) {
                        int id = packetContainer.getIntegers().read(0);

                        if (TEXT_LINE_SET.contains(id)) {
                            event.setCancelled(true);

                            PacketContainer fakePacketContainer = new PacketContainer(PacketType.Play.Client
                                    .BLOCK_PLACE);
                            fakePacketContainer.getHands().write(0, packetContainer.getHands()
                                    .read(0));

                            try {
                                manager.recieveClientPacket(event.getPlayer(), fakePacketContainer);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                ArenaApi.warning(String.format("Error blocking player interact at entity with id %s:" +
                                        " %s.", id, e.getMessage()));
                            }
                        }
                    }
                }
            }
        });
    }

    public HologramReplacement(Location location, double lineSpace) {
        this.arenaApi = ArenaApi.getInstance();
        this.rootLocation = location;
        this.lineSpace = lineSpace;
    }

    public HologramReplacement(Location location) {
        this(location, DEFAULT_LINE_SPACE);
    }

    public void addLine(String text) {
        TextLine textLine = createTextLine(
                rootLocation.clone().subtract(0, lineSpace * hologramLines.size(), 0),
                text
        );
        hologramLines.add(textLine);
        TEXT_LINE_SET.add(textLine.getEntityId());
    }

    private TextLine createTextLine(Location location, String text) {
        TextLine textLine = new TextLine(location);
        textLine.setVisualForEveryone(text);

        return textLine;
    }

    public void addLine(Material material) {
        hologramLines.add(
                createItemLine(rootLocation.clone().subtract(0, lineSpace * hologramLines.size(), 0), material)
        );
    }

    private ItemLine createItemLine(Location location, Material material) {
        ItemLine itemLine = new ItemLine(location);
        itemLine.setVisualForEveryone(material);

        return itemLine;
    }

    public void updateLineForEveryone(int index, String text) {
        HologramLine<?> hologramLine = hologramLines.get(index);
        if (hologramLine instanceof TextLine) {
            ((TextLine) hologramLine).setVisualForEveryone(text);
        } else {

        }
    }

    public void updateLine(int index, String text) {
        HologramLine<?> hologramLine = hologramLines.get(index);
        if (hologramLine instanceof TextLine) {
            ((TextLine) hologramLine).setVisual(text);
        } else {

        }
    }

    public void updateLineForPlayer(Player player, int index, String text) {
        HologramLine<?> hologramLine = hologramLines.get(index);
        if (hologramLine instanceof TextLine) {
            ((TextLine) hologramLine).setVisualForPlayer(player, text);
        } else {

        }
    }

    public void updateLineForEveryone(int index, Material material) {
        HologramLine<?> hologramLine = hologramLines.get(index);
        if (hologramLine instanceof ItemLine) {
            ((ItemLine) hologramLine).setVisualForEveryone(material);
        } else {

        }
    }

    public void updateLine(int index, Material material) {
        HologramLine<?> hologramLine = hologramLines.get(index);
        if (hologramLine instanceof ItemLine) {
            ((ItemLine) hologramLine).setVisual(material);
        } else {

        }
    }

    public void updateLineForPlayer(Player player, int index, Material material) {
        HologramLine<?> hologramLine = hologramLines.get(index);
        if (hologramLine instanceof ItemLine) {
            ((ItemLine) hologramLine).setVisualForPlayer(player, material);
        } else {

        }
    }

    public void renderToPlayer(Player player) {
        for (HologramLine<?> hologramLine : hologramLines) {
            hologramLine.createVisualForPlayer(player);
            hologramLine.updateVisualForPlayer(player);
        }
    }

    public void destroy() {
        int idCount = hologramLines.size();

        int[] ids = new int[idCount];
        for (int i = 0; i < idCount; i++) {
            ids[i] = hologramLines.get(0).getEntityId();
            hologramLines.remove(0);
        }

        for (Player player : rootLocation.getWorld().getPlayers()) {
            PacketContainer killPacketContainer = getKillPacketContainer(ids);
            arenaApi.sendPacketToPlayer(player, killPacketContainer);
        }

        TEXT_LINE_SET.clear();
    }

    private PacketContainer getKillPacketContainer(int id) {
        return getKillPacketContainer(new int[]{ id });
    }

    private PacketContainer getKillPacketContainer(int[] ids) {
        PacketContainer killPacketContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        killPacketContainer.getIntegerArrays().write(0, ids);

        return killPacketContainer;
    }

}
