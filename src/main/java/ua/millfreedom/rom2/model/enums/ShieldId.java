package ua.millfreedom.rom2.model.enums;

public enum ShieldId {
    EMPTY(0, ""),
    BUCKLER(1, "Buckler"),
    SMALL(2, "Small"),
    SOFT_SMALL(3, "Soft Small"),
    WOODEN_SMALL(4, "Wooden Small"),
    LARGE(5, "Large"),
    SOFT_LARGE(6, "Soft Large"),
    WOODEN_LARGE(7, "Wooden Large"),
    TOWER(8, "Tower"),
    WOODEN_TOWER(9, "Wooden Tower"),
    UNKNOWN(-1, "unknown");

    private static final ShieldId[] BY_ID = createLookup();

    public final int id;
    public final String tableName;

    ShieldId(int id, String tableName) {
        this.id = id;
        this.tableName = tableName;
    }

    public static ShieldId fromId(int id) {
        if (id < 0 || id >= BY_ID.length) {
            return UNKNOWN;
        }
        ShieldId value = BY_ID[id];
        return value == null ? UNKNOWN : value;
    }

    public boolean isBetween(ShieldId from, ShieldId to) {
        return id >= from.id && id <= to.id;
    }

    private static ShieldId[] createLookup() {
        int maxId = WOODEN_TOWER.id;
        ShieldId[] values = new ShieldId[maxId + 1];
        for (ShieldId value : ShieldId.values()) {
            if (value.id >= 0) {
                values[value.id] = value;
            }
        }
        return values;
    }
}
