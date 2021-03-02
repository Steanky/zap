package io.github.zap.zombies.proxy;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import io.github.zap.arenaapi.proxy.NMSProxy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

/**
 * Access NMS classes through this proxy.
 */
public interface ZombiesNMSProxy extends NMSProxy {
    NavigationProxy getNavigationFor(Mob mob);

    /**
     * Gets a wrapped signed property of a player's skin texture
     * @param player The player to get the skin from
     * @return A wrapped sign property of the texture
     */
    WrappedSignedProperty getSkin(Player player);
}
