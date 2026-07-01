package ua.millfreedom.rom2.model.color;

public class Consts {
    public static final byte RBITS = 5;
    public static final byte RSHIFT = 11;
    public static final byte R_COUNT = 1 << RBITS;
    public static final byte RBITMASK = R_COUNT - 1;
    public static final byte RSHIFTCOUNT = 8 - RBITS;

    public static final byte GBITS = 6;
    public static final byte GSHIFT = 5;
    public static final byte G_COUNT = 1 << GBITS;
    public static final byte GBITMASK = G_COUNT - 1;
    public static final byte GSHIFTCOUNT = 8 - GBITS;

    public static final byte BBITS = 5;
    public static final byte BSHIFT = 0;
    public static final byte B_COUNT = 1 << BBITS;
    public static final byte BBITMASK = B_COUNT - 1;
    public static final byte BSHIFTCOUNT = 8 - BBITS;

    // half-LSB of the dropped part (for rounding)
    public static final byte RROUND = (RSHIFTCOUNT > 0) ? (1 << (RSHIFTCOUNT - 1)) : 0;
    public static final byte GROUND = (GSHIFTCOUNT > 0) ? (1 << (GSHIFTCOUNT - 1)) : 0;
    public static final byte BROUND = (BSHIFTCOUNT > 0) ? (1 << (BSHIFTCOUNT - 1)) : 0;

    /**
     * Native support extracted from InitLUT @0045225B g_renderEffectTable initialization.
     */
    public static final byte[][] RENDER_EFFECT_TABLE = new byte[0x80][0x80];

    /**
     * Native support extracted from InitLUT @0045225B g_midGray565 initialization.
     */
    public static final short MIDGRAY565 = 0x7BEF;

    static {
        if (!(RBITMASK > 0 && GBITMASK > 0 && BBITMASK > 0)) {
            throw new RuntimeException("You'd better fix this!");
        }
        initRenderEffectTable();
    }

    /**
     * Native support extracted from InitLUT @0045225B g_renderEffectTable loop.
     * Fully ported.
     */
    private static void initRenderEffectTable() {
        for (int outer = 1; outer < 128; outer++) {
            final int step = 0x200000 / (outer + 1); // fixed-point step
            int accum = 0;

            for (int inner = 0; inner <= outer; inner++) {
                accum += step;
                RENDER_EFFECT_TABLE[outer][inner] = (byte) ((accum + 0x8000) >> 16); // rounded
            }
        }
    }

}
