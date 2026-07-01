package ua.millfreedom.rom2.model.column;

public enum MagicItemColumn {
    PRICE(0),
    WEIGHT(1),
    EFFECTS(2);

    public final int index;

    MagicItemColumn(int index) {
        this.index = index;
    }

    public static MagicItemColumn from(int index) {
        for (MagicItemColumn column : MagicItemColumn.values()) {
            if (column.index == index) {
                return column;
            }
        }
        throw new RuntimeException("no such column");
    }
}
