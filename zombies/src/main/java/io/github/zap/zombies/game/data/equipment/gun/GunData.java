package io.github.zap.zombies.game.data.equipment.gun;

import io.github.zap.arenaapi.localization.LocalizationManager;
import io.github.zap.zombies.MessageKey;
import io.github.zap.zombies.game.data.equipment.EquipmentData;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Data for a gun
 * @param <L> The gun level type
 */
@Getter
public class GunData<L extends GunLevel> extends EquipmentData<L> {

    private final transient String unchangedFormat = ChatColor.DARK_GRAY + " ◼ " + ChatColor.GRAY + "%s: "
            + ChatColor.GREEN + "%s";

    private final transient String changedFormat = ChatColor.DARK_GRAY + " ◼ " + ChatColor.GRAY + "%s: "
            + ChatColor.DARK_GRAY + "%s ➔ " + ChatColor.GREEN + "%s";

    public GunData(String type, String name, String displayName, List<String> lore, List<L> levels, Material material) {
        super(type, name, displayName, lore, levels, material);
    }

    protected GunData() {

    }

    @Override
    public List<String> getLore(Player player, int level) {
        List<String> lore = super.getLore(player, level);

        lore.add("");
        lore.addAll(getStatsLore(level));
        lore.add("");

        LocalizationManager localizationManager = getLocalizationManager();
        Locale locale = localizationManager.getPlayerLocale(player);
        lore.add(ChatColor.YELLOW + localizationManager.getLocalizedMessage(locale, MessageKey.LEFT_CLICK.getKey())
                + " " + ChatColor.GRAY + localizationManager.getLocalizedMessage(locale, MessageKey.TO_RELOAD.getKey())
                + ".");
        lore.add(ChatColor.YELLOW + localizationManager.getLocalizedMessage(locale, MessageKey.RIGHT_CLICK.getKey())
                + " " + ChatColor.GRAY + localizationManager.getLocalizedMessage(locale, MessageKey.TO_SHOOT.getKey())
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
                statsLore.add(String.format(unchangedFormat, "Fire Rate", current.getFireRate() + "s"));
            } else {
                statsLore.add(String.format(changedFormat, "Fire Rate", previous.getFireRate() + "s",
                        current.getFireRate() + "s"));
            }
            if (previous.getReloadRate() == current.getReloadRate()) {
                statsLore.add(String.format(unchangedFormat, "Reload Rate", current.getReloadRate() + "s"));
            } else {
                statsLore.add(String.format(changedFormat, "Reload Rate", previous.getReloadRate() + "s",
                        current.getReloadRate() + "s"));
            }
        } else {
            statsLore.add(String.format(unchangedFormat, "Damage", current.getDamage() + " HP"));
            statsLore.add(String.format(unchangedFormat, "Ammo", current.getAmmo()));
            statsLore.add(String.format(unchangedFormat, "Clip Ammo", current.getClipAmmo()));
            statsLore.add(String.format(unchangedFormat, "Fire Rate", current.getFireRate() + "s"));
            statsLore.add(String.format(unchangedFormat, "Reload Rate", current.getReloadRate() + "s"));
        }

        return statsLore;
    }

    @Override
    public ChatColor getDefaultChatColor() {
        return ChatColor.GOLD;
    }

}
