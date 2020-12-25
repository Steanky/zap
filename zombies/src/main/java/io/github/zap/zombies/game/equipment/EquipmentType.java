package io.github.zap.zombies.game.equipment;

/**
 * Utility enum to represent types of equipment
 */
public enum EquipmentType {

    MELEE("melee"),
    GUN("gun"),
    SKILL("skill"),
    PERK("perk");

    private final String name;

    EquipmentType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
