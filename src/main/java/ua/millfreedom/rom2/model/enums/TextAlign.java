package ua.millfreedom.rom2.model.enums;

/**
 * Native support: shared `TEXT_ALIGN` bitmask values reused by UI font draw paths.
 */
public enum TextAlign {
    DEFAULT(0x0),
    RIGHT(0x1),
    CENTER(0x2),
    BOTTOM(0x4),
    VERTICAL_CENTER(0x8);

    public final int mask;

    /**
     * not ported.
     */
    TextAlign(int mask) {
        this.mask = mask;
    }

    /**
     * not ported.
     */
    public static int combine(TextAlign... values) {
        int result = 0;
        for (TextAlign value : values) {
            result |= value.mask;
        }
        return result;
    }

    /**
     * Java helper for testing whether an arbitrary native `TEXT_ALIGN` flag set contains this value.
     * not ported.
     */
    public boolean matches(int alignFlags) {
        if (mask == 0) {
            return alignFlags == 0;
        }
        return (alignFlags & mask) == mask;
    }
}
