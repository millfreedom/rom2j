package ua.millfreedom.rom2.model.enums;

public enum WeaponId {
    EMPTY(0, ""),
    BAREHANDS(1, "BareHands"),
    DAGGER(2, "Dagger"),
    SHORT_SWORD(3, "Short Sword"),
    LONG_SWORD(4, "Long Sword"),
    BASTARD_SWORD(5, "Bastard Sword"),
    TWO_HANDED_SWORD(6, "Two Handed Sword"),
    CLUB(7, "Club"),
    SPIKED_CLUB(8, "Spiked Club"),
    MACE(9, "Mace"),
    MORNING_STAR(10, "Morning Star"),
    PICK_HAMMER(11, "Pick Hammer"),
    WAR_HAMMER(12, "War Hammer"),
    STAFF(13, "Staff"),
    SHAMAN_STAFF(14, "Shaman Staff"),
    PIKE(15, "Pike"),
    HALBERD(16, "Halberd"),
    LANCE(17, "Lance"),
    AXE(18, "Axe"),
    TWO_HANDED_AXE(19, "Two Handed Axe"),
    SHORT_BOW(20, "Short Bow"),
    LONG_BOW(21, "Long Bow"),
    CROSSBOW(22, "Crossbow"),
    REM(23, "rem"),
    SONIC_BEAM(24, "Sonic Beam"),
    FLAME_THROWER(25, "Flame Thrower"),
    BOULDER_THROWER(26, "Boulder Thrower"),
    PLASMA_SWORD(27, "Plasma Sword"),
    UNKNOWN(-1, "unknown");

    private static final WeaponId[] BY_ID = createLookup();

    public final int id;
    public final String tableName;

    WeaponId(int id, String tableName) {
        this.id = id;
        this.tableName = tableName;
    }

    public static WeaponId fromId(int id) {
        if (id < 0 || id >= BY_ID.length) {
            return UNKNOWN;
        }
        WeaponId value = BY_ID[id];
        return value == null ? UNKNOWN : value;
    }

    public boolean isBetween(WeaponId from, WeaponId to) {
        return id >= from.id && id <= to.id;
    }

    private static WeaponId[] createLookup() {
        int maxId = PLASMA_SWORD.id;
        WeaponId[] values = new WeaponId[maxId + 1];
        for (WeaponId value : WeaponId.values()) {
            if (value.id >= 0) {
                values[value.id] = value;
            }
        }
        return values;
    }
}
