package io.github.zap.zombies.game.mob.goal;

import lombok.Value;
import net.minecraft.server.v1_16_R3.AttributeBase;

@Value
public class AttributeValue {
    AttributeBase attribute;
    double value;
}
