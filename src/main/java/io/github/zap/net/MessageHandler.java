package io.github.zap.net;

import com.google.common.io.ByteArrayDataInput;
import org.bukkit.entity.Player;

public interface MessageHandler {
    void handle(Player player, ByteArrayDataInput data);
}
