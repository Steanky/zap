package io.github.zap.zombies.game.data.util;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_16_R3.MojangsonParser;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.Material;

@Getter
@Setter
public class ItemStackDescription {
    Material material;
    int count = 1;
    String nbt;
}
