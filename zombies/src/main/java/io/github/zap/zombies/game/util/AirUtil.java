package io.github.zap.zombies.game.util;

import com.google.common.collect.Sets;
import org.bukkit.Material;

import java.util.Set;

/**
 * Utils for dealing with air and its 2 deadly brothers
 */
public class AirUtil {

    public final static Set<Material> AIR_MATERIALS =
            Sets.newHashSet(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);

}
