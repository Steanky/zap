package io.github.zap.net;

import com.google.common.io.ByteArrayDataInput;
import org.bukkit.entity.Player;

public class BungeeHandler implements MessageHandler {
    @Override
    public boolean handle(Player player, byte[] message) {
        //TODO: handle bungeecord messages
        return false;
    }
}
