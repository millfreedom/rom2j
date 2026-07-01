package ua.millfreedom.rom2.model.enums;

public enum MaterialId {
    IRON(0, "Iron"),
    BRONZE(1, "Bronze"),
    STEEL(2, "Steel"),
    SILVER(3, "Silver"),
    GOLD(4, "Gold"),
    MITHRILL(5, "Mithrill"),
    ADAMANTIUM(6, "Adamantium"),
    METEORIC(7, "Meteoric"),
    WOOD(8, "Wood"),
    MAGIC_WOOD(9, "Magic Wood"),
    LEATHER(10, "Leather"),
    HARD_LEATHER(11, "Hard Leather"),
    DRAGON_LEATHER(12, "Dragon Leather"),
    BONE(13, "Bone"),
    CRYSTAL(14, "Crystal"),
    NONE(15, "None"),
    UNKNOWN(-1, "unknown");

    private static final MaterialId[] BY_ID = createLookup();

    public final int id;
    public final String tableName;

    MaterialId(int id, String tableName) {
        this.id = id;
        this.tableName = tableName;
    }

    public static MaterialId fromId(int id) {
        if (id < 0 || id >= BY_ID.length) {
            return UNKNOWN;
        }
        MaterialId value = BY_ID[id];
        return value == null ? UNKNOWN : value;
    }

    public boolean isBetween(MaterialId from, MaterialId to) {
        return id >= from.id && id <= to.id;
    }

    private static MaterialId[] createLookup() {
        int maxId = NONE.id;
        MaterialId[] values = new MaterialId[maxId + 1];
        for (MaterialId value : MaterialId.values()) {
            if (value.id >= 0) {
                values[value.id] = value;
            }
        }
        return values;
    }
}
