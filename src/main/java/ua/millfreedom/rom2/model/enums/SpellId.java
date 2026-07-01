package ua.millfreedom.rom2.model.enums;

public enum SpellId {
    EMPTY(0, ""),
    FIRE_ARROW(1, "Fire Arrow"),
    FIRE_BALL(2, "Fire Ball"),
    WALL_OF_FIRE(3, "Wall of Fire"),
    PROTECTION_FROM_FIRE(4, "Protection from Fire"),
    ICE_MISSILE(5, "Ice Missile"),
    POISON_CLOUD(6, "Poison Cloud"),
    BLIZZARD(7, "Blizzard"),
    PROTECTION_FROM_WATER(8, "Protection from Water"),
    ACID_STREAM(9, "Acid Stream"),
    LIGHTNING(10, "Lightning"),
    PRISMATIC_SPRAY(11, "Prismatic Spray"),
    INVISIBILITY(12, "Invisibility"),
    PROTECTION_FROM_AIR(13, "Protection from Air"),
    DARKNESS(14, "Darkness"),
    LIGHT(15, "Light"),
    DIAMOND_DUST(16, "Diamond Dust"),
    WALL_OF_EARTH(17, "Wall of Earth"),
    STONE_CURSE(18, "Stone Curse"),
    PROTECTION_FROM_EARTH(19, "Protection from Earth"),
    BLESS(20, "Bless"),
    HASTE(21, "Haste"),
    CONTROL_SPIRIT(22, "Control Spirit"),
    TELEPORT(23, "Teleport"),
    HEAL(24, "Heal"),
    SUMMON(25, "Summon"),
    DRAIN_LIFE(26, "Drain Life"),
    SHIELD(27, "Shield"),
    CURSE(28, "Curse"),
    SLOW(29, "Slow"),
    EMPTY_30(30, ""),
    EMPTY_31(31, ""),
    EMPTY_32(32, ""),
    EMPTY_33(33, ""),
    EMPTY_34(34, ""),
    UNKNOWN(-1, "unknown");

    private static final SpellId[] BY_ID = createLookup();

    public final int id;
    public final String tableName;

    SpellId(int id, String tableName) {
        this.id = id;
        this.tableName = tableName;
    }

    public static SpellId fromId(int id) {
        if (id < 0 || id >= BY_ID.length) {
            return UNKNOWN;
        }
        SpellId value = BY_ID[id];
        return value == null ? UNKNOWN : value;
    }

    public boolean isBetween(SpellId from, SpellId to) {
        return id >= from.id && id <= to.id;
    }

    private static SpellId[] createLookup() {
        int maxId = EMPTY_34.id;
        SpellId[] values = new SpellId[maxId + 1];
        for (SpellId value : SpellId.values()) {
            if (value.id >= 0) {
                values[value.id] = value;
            }
        }
        return values;
    }
}
