package io.github.zap.zombies.game2.corpse;

import io.github.zap.zombies.game2.corpse.message.CorpseMessager;
import io.github.zap.zombies.game2.corpse.visual.CorpseVisual;
import io.github.zap.zombies.game2.player.ZombiesPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Corpse {

    private final ZombiesPlayer player;

    private final CorpseVisual visual;

    private final CorpseMessager messager;

    private final ItemStack[] armor = new ItemStack[4];

    private final Location location;

    private final long deathTime;

    private ZombiesPlayer reviver;

    private long timeUntilDeath;

    private long timeUntilRevival;

    private boolean active = true;

    public Corpse(@NotNull ZombiesPlayer player, @NotNull CorpseVisual visual, @NotNull CorpseMessager messager,
                  long deathTime) {
        Optional<Player> bukkitPlayerOptional = player.getPlayerView().getPlayerIfValid();
        if (bukkitPlayerOptional.isEmpty()) {
            throw new IllegalArgumentException("Tried to create a corpse for an offline player!");
        }
        Player bukkitPlayer = bukkitPlayerOptional.get();

        this.player = player;
        this.visual = visual;
        this.messager = messager;
        player.getArmorHolder().getArmor(this.armor);
        this.location = bukkitPlayer.getLocation();
        this.deathTime = deathTime;
    }

    public void setReviver(@Nullable ZombiesPlayer reviver) {
        if (!active) {
            return;
        }

        this.reviver = reviver;
        if (reviver != null) {

        }
        else {

        }
    }

    private void continueDying() {
        if (!active) {
            return;
        }
        if (timeUntilDeath <= 0) {

        }
        else {

            timeUntilDeath -= 2;
        }
    }

}
