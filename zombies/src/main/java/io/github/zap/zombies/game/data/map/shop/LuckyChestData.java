package io.github.zap.zombies.game.data.map.shop;

import io.github.zap.arenaapi.hologram.Hologram;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import io.github.zap.zombies.game.shop.LuckyChest;
import io.github.zap.zombies.game.util.Jingle;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Data for a lucky chest
 */
@Getter
public class LuckyChestData extends ShopData {

    private List<String> equipments = new ArrayList<>();

    private List<ImmutablePair<List<Jingle.Note>, Long>> jingle;

    private Vector chestLocation;

    private int cost;

    private int rollsUntilMove;

    private long sittingTime;

    private LuckyChestData() {

    }

}
