package ua.millfreedom.rom2.model.enums;

public enum ArmorId {
    EMPTY(0, ""),
    RING(1, "Ring"),
    AMULET(2, "Amulet"),
    HAT(3, "Hat"),
    CAP(4, "Cap"),
    LOW_HAT(5, "Low Hat"),
    HELM(6, "Helm"),
    SOFT_HELM(7, "Soft Helm"),
    CHAIN_HELM(8, "Chain Helm"),
    FULL_HELM(9, "Full Helm"),
    PLATE_HELM(10, "Plate Helm"),
    CLOAK(11, "Cloak"),
    CAPE(12, "Cape"),
    ROBE(13, "Robe"),
    DRESS(14, "Dress"),
    SOFT_MAIL(15, "Soft Mail"),
    CHAIN_MAIL(16, "Chain Mail"),
    SCALE_MAIL(17, "Scale Mail"),
    CUIRASS(18, "Cuirass"),
    PLATE_CUIRASS(19, "Plate Cuirass"),
    BRACERS(20, "Bracers"),
    SOFT_BRACERS(21, "Soft Bracers"),
    PLATE_BRACERS(22, "Plate Bracers"),
    GLOVES(23, "Gloves"),
    SOFT_GAUNTLETS(24, "Soft Gauntlets"),
    CHAIN_GAUNTLETS(25, "Chain Gauntlets"),
    SCALE_GAUNTLETS(26, "Scale Gauntlets"),
    SHOES(27, "Shoes"),
    SOFT_BOOTS(28, "Soft Boots"),
    CHAIN_BOOTS(29, "Chain Boots"),
    PLATE_BOOTS(30, "Plate Boots"),
    UNKNOWN(-1, "unknown");

    private static final ArmorId[] BY_ID = createLookup();

    public final int id;
    public final String tableName;

    ArmorId(int id, String tableName) {
        this.id = id;
        this.tableName = tableName;
    }

    public static ArmorId fromId(int id) {
        if (id < 0 || id >= BY_ID.length) {
            return UNKNOWN;
        }
        ArmorId value = BY_ID[id];
        return value == null ? UNKNOWN : value;
    }

    public boolean isBetween(ArmorId from, ArmorId to) {
        return id >= from.id && id <= to.id;
    }

    private static ArmorId[] createLookup() {
        int maxId = PLATE_BOOTS.id;
        ArmorId[] values = new ArmorId[maxId + 1];
        for (ArmorId value : ArmorId.values()) {
            if (value.id >= 0) {
                values[value.id] = value;
            }
        }
        return values;
    }
}
