package ua.millfreedom.rom2.model.column;

public enum UnitColumn {
    BODY(0),
    REACTION(1),
    MIND(2),
    SPIRIT(3),
    HEALTH_MAX(4),
    HP_REGENERATION(5),
    MANA_MAX(6),
    MP_REGENERATION(7),
    SPEED(8),
    ROTATION_SPEED(9),
    SCAN_RANGE(10),
    PHYSICAL_MIN(11),
    PHYSICAL_MAX(12),
    ATTACK_KIND(13),
    TO_HIT(14),
    DEFENCE(15),
    ABSORPTION(16),
    ATTACK_CHARGE_TIME(17),
    ATTACK_RELAX_TIME(18),
    PROTECTION_FIRE(19),
    PROTECTION_WATER(20),
    PROTECTION_AIR(21),
    PROTECTION_EARTH(22),
    PROTECTION_ASTRAL(23),
    RESISTANCE_BLADE(24),
    RESISTANCE_AXE(25),
    RESISTANCE_BLUDGEON(26),
    RESISTANCE_PIKE(27),
    RESISTANCE_SHOOTING(28),
    TYPE_ID(29),
    FACE(30),
    TOKEN_SIZE(31),
    MOVEMENT_TYPE(32),
    DYING_TIME(33),
    WITHDRAW(34),
    WIMPY(35),
    SEE_INVISIBLE(36),
    XP_VALUE(37),
    TREASURE1_GOLD(38),
    TREASURE1_MIN(39),
    TREASURE1_MAX(40),
    TREASURE2_ITEM(41),
    TREASURE2_MIN(42),
    TREASURE2_MAX(43),
    TREASURE2_MASK(44),
    NOT_USED(45),
    NOT_USED1(46),
    POWER(47),
    SPELL_1(48),
    SPELL_PROBABILITY_1(49),
    SPELL_2(50),
    SPELL_PROBABILITY_2(51),
    SPELL_3(52),
    SPELL_PROBABILITY_3(53),
    SPELL_POWER(54),
    SERVER_ID(55),
    KNOWN_SPELLS(56),
    SKILL_FIRE(57),
    SKILL_WATER(58),
    SKILL_AIR(59),
    SKILL_EARTH(60),
    SKILL_ASTRAL(61);

    public final int index;

    UnitColumn(int index) {
        this.index = index;
    }

    public static UnitColumn from(int index) {
        for (UnitColumn column : UnitColumn.values()) {
            if (column.index == index) {
                return column;
            }
        }
        throw new RuntimeException("no such column");
    }
}
