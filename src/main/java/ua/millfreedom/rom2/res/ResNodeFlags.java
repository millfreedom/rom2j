package ua.millfreedom.rom2.res;

// =========================================================================
// Flags (bitmask)
// =========================================================================
public final class ResNodeFlags {
    // not ported.
    private ResNodeFlags() {
    }

    public static final int RESFLAG_IS_CONTAINER = 0x00000001;
    public static final int RESFLAG_TYPE_INT32 = 0x00000002;
    public static final int RESFLAG_TYPE_DOUBLE = 0x00000004;
    public static final int RESFLAG_TYPE_INT32_ARRAY = 0x00000006;
    public static final int RESFLAG_TYPE_BLOB = 0x00000008;
    public static final int RESFLAG_TYPE_DOUBLE_ARRAY = 0x0000000A;
    public static final int RESFLAG_MASK_ANY_OF_BASIC = 0x0000000E;
    public static final int RESFLAG_SORTED = 0x00000010;
    public static final int RESFLAG_FLAT_ARRAY_TREE = 0x00000011;
    public static final int RESFLAG_NAME_TRUNCATED = 0x10000000;
    public static final int RESTYPE_UNKNOWN2 = 0x20000000;
    public static final int RESFLAG_VISITED = 0x40000000;
    public static final int RESFLAG_NODES_AFTER_HEADER = 0x80000000;
}
