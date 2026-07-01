package ua.millfreedom.rom2.model.column;

public enum HumanColumn {
    BODY(0),
    REACTION(1),
    MIND(2),
    SPIRIT(3),
    HEALTH_MAX(4),
    MANA_MAX(5),
    SPEED(6),
    ROTATION_SPEED(7),
    SCAN_RANGE(8),
    DEFENCE(9),
    SKILL_GENERAL(10),
    SKILL_BLADE_FIRE(11),
    SKILL_AXE_WATER(12),
    SKILL_BLUDGEON_AIR(13),
    SKILL_PIKE_EARTH(14),
    SKILL_SHOOTING_ASTRAL(15),
    TYPE_ID(16),
    FACE(17),
    IS_FEMALE(18),
    ATTACK_CHARGE_TIME(19),
    ATTACK_RELAX_TIME(20),
    TOKEN_SIZE(21),
    MOVEMENT_TYPE(22),
    DYING_TIME(23),
    SERVER_ID(24),
    KNOWN_SPELLS(25);

    public final int index;

    HumanColumn(int index) {
        this.index = index;
    }

    public static HumanColumn from(int index) {
        for (HumanColumn column : HumanColumn.values()) {
            if (column.index == index) {
                return column;
            }
        }
        throw new RuntimeException("no such column");
    }
}
