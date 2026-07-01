package ua.millfreedom.rom2.model.column;

public enum WorldItemColumn {
    SHAPE(0),
    MATERIAL(1),
    PRICE(2),
    WEIGHT(3),
    SLOT(4),
    ATTACK_TYPE(5),
    PHYSICAL_MIN(6),
    PHYSICAL_MAX(7),
    TO_HIT(8),
    DEFENCE(9),
    ABSORPTION(10),
    RANGE(11),
    CHARGE(12),
    RELAX(13),
    TWO_HANDED(14),
    SUITABLE_FOR(15),
    OTHER_PARAMETER(16);

    public final int index;

    WorldItemColumn(int index) {
        this.index = index;
    }

    public static WorldItemColumn from(int index) {
        for (WorldItemColumn column : WorldItemColumn.values()) {
            if (column.index == index) {
                return column;
            }
        }
        throw new RuntimeException("no such column");
    }
}
