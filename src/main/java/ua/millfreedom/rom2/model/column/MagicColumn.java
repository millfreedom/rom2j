package ua.millfreedom.rom2.model.column;

public enum MagicColumn {
    COST_MP(0),
    AFFECT_MIN(1),
    AFFECT_MAX(2),
    USABLE_BY(3),
    IN_WEAPON_A(4),
    IN_SHIELD(5),
    IN_NOT_USED3_A(6),
    IN_RING_A(7),
    IN_AMULET_A(8),
    IN_HELM(9),
    IN_MAIL(10),
    IN_CUIRASS(11),
    IN_BRACERS(12),
    IN_GAUNTLETS(13),
    IN_NOT_USED11_A(14),
    IN_BOOTS(15),
    IN_WEAPON_B(16),
    IN_NOT_USED_B(17),
    IN_NOT_USED3_B(18),
    IN_RING_B(19),
    IN_AMULET_B(20),
    IN_HAT(21),
    IN_ROBE(22),
    IN_CLOAK(23),
    IN_NOT_USED_C(24),
    IN_GLOVES(25),
    IN_NOT_USED11_B(26),
    IN_SHOES(27);

    public final int index;

    MagicColumn(int index) {
        this.index = index;
    }

    public static MagicColumn from(int index) {
        for (MagicColumn column : MagicColumn.values()) {
            if (column.index == index) {
                return column;
            }
        }
        throw new RuntimeException("no such column");
    }
}
