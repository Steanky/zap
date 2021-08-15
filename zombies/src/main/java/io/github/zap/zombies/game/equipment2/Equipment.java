package io.github.zap.zombies.game.equipment2;

import io.github.zap.arenaapi.hotbar2.HotbarManager;
import io.github.zap.arenaapi.hotbar2.HotbarObjectBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Equipment extends HotbarObjectBase {

    private final List<EquipmentLevel> levels;

    private EquipmentLevel level;

    private int levelIndex = 0;

    public Equipment(@NotNull HotbarManager hotbarManager, boolean selected, @NotNull List<EquipmentLevel> levels) {
        super(hotbarManager, null, selected);

        this.levels = levels;
        this.level = levels.get(levelIndex);
    }

    public @NotNull List<EquipmentLevel> getLevels() {
        return new ArrayList<>(levels);
    }

    public int getLevelIndex() {
        return levelIndex;
    }

    public void upgrade() {
        if (levelIndex < levels.size()) {
            level = levels.get(++levelIndex);
            currentStack = level.features().get(level.visualFeatureIndex()).getVisual(this);
            redraw();
        }
        else throw new IllegalStateException("The equipment is already at its max level!");
    }

    public void downgrade() {
        if (levelIndex > 0) {
            level = levels.get(--levelIndex);
            currentStack = level.features().get(level.visualFeatureIndex()).getVisual(this);
            redraw();
        }
        else throw new IllegalStateException("The equipment is already at level " + levelIndex + "!");
    }

}
