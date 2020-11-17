package io.github.zap.arenaapi.event;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.plugin.Plugin;

@EqualsAndHashCode(callSuper = false)
@Value
public class PreDisableEvent extends CustomEvent {
    Plugin plugin;
}
