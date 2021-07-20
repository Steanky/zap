package io.github.zap.nms.v1_16_R3.player;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.zap.nms.common.player.PlayerBridge;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class PlayerBridge_v1_16_R3 implements PlayerBridge {

    public static final PlayerBridge_v1_16_R3 INSTANCE = new PlayerBridge_v1_16_R3();

    private PlayerBridge_v1_16_R3() {}

    @Override
    public @Nullable WrappedSignedProperty getSkin(@NotNull Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        GameProfile gameProfile = craftPlayer.getProfile();
        if (gameProfile != null) {
            Collection<Property> texture = gameProfile.getProperties().get("textures");

            if (texture.size() > 0) {
                return WrappedSignedProperty.fromHandle(texture.iterator().next());
            }
        }

        return null;
    }

}
