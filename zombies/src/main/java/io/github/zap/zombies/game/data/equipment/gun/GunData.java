package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.arenaapi.util.TimeUtil;
import io.github.zap.zombies.game.data.equipment.UltimateableData;
import io.github.zap.zombies.game.equipment.EquipmentType;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a gun
 * @param <L> The gun level type
 */
@Getter
public class GunData<L extends GunLevel> extends UltimateableData<L> {

    private Sound sound;

    private final transient String unchangedFormat = ChatColor.DARK_GRAY + " ◼ " + ChatColor.GRAY + "%s: "
            + ChatColor.GREEN + "%s";

    private final transient String changedFormat = ChatColor.DARK_GRAY + " ◼ " + ChatColor.GRAY + "%s: "
            + ChatColor.DARK_GRAY + "%s ➔ " + ChatColor.GREEN + "%s";

    @Override
    public List<String> getLore(Player player, int level) {
        List<String> lore = super.getLore(player, level);

        lore.add("");
        lore.addAll(getStatsLore(level));
        lore.add("");

        lore.add(ChatColor.YELLOW + "Left Click"
                + " " + ChatColor.GRAY + "to Reload"
                + ".");
        lore.add(ChatColor.YELLOW + "Right Click"
                + " " + ChatColor.GRAY + "to Shoot"
                + ".");

        return lore;
    }

    private List<String> getStatsLore(int level) {
        List<String> statsLore = new ArrayList<>();
        GunLevel current = getLevels().get(level);

        // TODO: make this not bad code
        if (level > 0) {
            GunLevel previous = getLevels().get(level - 1);

            if (previous.getDamage() == current.getDamage()) {
                statsLore.add(String.format(unchangedFormat, "Damage", current.getDamage() + " HP"));
            } else {
                statsLore.add(String.format(changedFormat, "Damage", previous.getDamage() + " HP", current.getDamage()
                        + " HP"));
            }
            if (previous.getAmmo() == current.getAmmo()) {
                statsLore.add(String.format(unchangedFormat, "Ammo", current.getAmmo()));
            } else {
                statsLore.add(String.format(changedFormat, "Ammo", previous.getAmmo(), current.getAmmo()));
            }
            if (previous.getClipAmmo() == current.getClipAmmo()) {
                statsLore.add(String.format(unchangedFormat, "Clip Ammo", current.getClipAmmo()));
            } else {
                statsLore.add(String.format(changedFormat, "Clip Ammo", previous.getClipAmmo(), current.getClipAmmo()));
            }
            if (previous.getFireRate() == current.getFireRate()) {
                statsLore.add(String.format(unchangedFormat, "Fire Rate",
                        TimeUtil.convertTicksToSeconds(current.getFireRate()) + "s"));
            } else {
                statsLore.add(String.format(changedFormat, "Fire Rate",
                        TimeUtil.convertTicksToSeconds(previous.getFireRate()) + "s",
                        TimeUtil.convertTicksToSeconds(current.getFireRate()) + "s"));
            }
            if (previous.getReloadRate() == current.getReloadRate()) {
                statsLore.add(String.format(unchangedFormat, "Reload Rate",
                        TimeUtil.convertTicksToSeconds(current.getReloadRate()) + "s"));
            } else {
                statsLore.add(String.format(changedFormat, "Reload Rate",
                        TimeUtil.convertTicksToSeconds(previous.getReloadRate()) + "s",
                        TimeUtil.convertTicksToSeconds(current.getReloadRate()) + "s"));
            }
        } else {
            statsLore.add(String.format(unchangedFormat, "Damage", current.getDamage() + " HP"));
            statsLore.add(String.format(unchangedFormat, "Ammo", current.getAmmo()));
            statsLore.add(String.format(unchangedFormat, "Clip Ammo", current.getClipAmmo()));
            statsLore.add(String.format(unchangedFormat, "Fire Rate",
                    TimeUtil.convertTicksToSeconds(current.getFireRate()) + "s"));
            statsLore.add(String.format(unchangedFormat, "Reload Rate",
                    TimeUtil.convertTicksToSeconds(current.getReloadRate()) + "s"));
        }

        return statsLore;
    }

    @Override
    public TextColor getDefaultChatColor() {
        return NamedTextColor.GOLD;
    }

    @Override
    public String getEquipmentType() {
        return EquipmentType.GUN.name();
    }
}
