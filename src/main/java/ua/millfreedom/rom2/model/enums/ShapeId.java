package ua.millfreedom.rom2.model.enums;

import ua.millfreedom.rom2.model.color.RGB32;

public enum ShapeId {
    COMMON(0, "Common", RGB32.from(0xFD, 0xFE, 0xFE), 1),
    UNCOMMON(1, "Uncommon", RGB32.from(0x27, 0xAE, 0x60), 2),
    RARE(2, "Rare", RGB32.from(0x24, 0x71, 0xA3), 3),
    VERY_RARE(3, "Very Rare", RGB32.from(0xF1, 0xC4, 0x0F), 4),
    ELVEN(4, "Elven", RGB32.from(0xdc, 0x23, 0x67), 5),
    BAD(5, "Bad", RGB32.from(0x24, 0x71, 0xA3), 4),
    GOOD(6, "Good", RGB32.from(0x27, 0xAE, 0x60), 5),
    UNKNOWN(-1, "unknown", RGB32.from(0xF1, 0xC4, 0x0F), 0);

    private static final ShapeId[] BY_ID = createLookup();

    public final int id;
    public final String tableName;
    public final RGB32 color;
    public final int rarity;

    ShapeId(int id, String tableName, RGB32 color, int rarity) {
        this.id = id;
        this.tableName = tableName;
        this.color = color;
        this.rarity = rarity;
    }

    public static ShapeId fromId(int id) {
        if (id < 0 || id >= BY_ID.length) {
            return UNKNOWN;
        }
        ShapeId value = BY_ID[id];
        return value == null ? UNKNOWN : value;
    }

    public boolean isBetween(ShapeId from, ShapeId to) {
        return id >= from.id && id <= to.id;
    }

    private static ShapeId[] createLookup() {
        int maxId = GOOD.id;
        ShapeId[] values = new ShapeId[maxId + 1];
        for (ShapeId value : ShapeId.values()) {
            if (value.id >= 0) {
                values[value.id] = value;
            }
        }
        return values;
    }
}
