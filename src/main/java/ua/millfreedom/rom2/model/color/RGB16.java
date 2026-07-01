package ua.millfreedom.rom2.model.color;

import static ua.millfreedom.rom2.console.Utils.color24;

/**
 * DO NOT USE constructor directly! Prefer RGB16.of(int) to reuse the same instance!
 * This class is a Java replacement for native LUT
 * The colours are INTENTIONALLY expanded further than native 565!
 *
 * @param val
 */
public record RGB16(short val) implements RGBA {

    private static final int CACHE_SIZE = 0x10000;
    private static final RGB32[] cache32 = new RGB32[CACHE_SIZE];
    private static final RGB16[] cache = initCache();
    public static final RGB16 BLACK = cache[0]; // same af .of(0)
    public static final RGB16 WHITE = cache[cache.length - 1]; // same af .of(0xFFFF)

    // not ported.
    private static RGB16[] initCache() {
        RGB16[] result = new RGB16[CACHE_SIZE];
        for (int r5 = 0; r5 < 32; r5++) {
            for (int g6 = 0; g6 < 64; g6++) {
                for (int b5 = 0; b5 < 32; b5++) {
                    int packed = pack565(r5, g6, b5);
                    RGB16 rgb16 = new RGB16((short) packed);
                    result[packed] = rgb16;
                    cache32[packed] = RGB32.from(rgb16.r(), rgb16.g(), rgb16.b());
                }
            }
        }
        return result;
    }

    // not ported.
    public static RGB16 of(int v565) {
        return getCached(v565);
    }

    // not ported.
    public static RGB16 from888(int rgb888) {
        return from(rgb888 >>> 16, rgb888 >>> 8, rgb888);
    }

    // not ported.
    public static RGB16 from(int r8, int g8, int b8) {
        return getCached(pack888(r8, g8, b8));
    }

    /**
     * Native support extracted from Global::PackRoundedRgbToDisplayPixel @00474427 for Java's fixed RGB565 renderer.
     * The r+4/g+2/b+4 expansion matches native rounded channel packing.
     */
    private static int pack888(int r8, int g8, int b8) {
        int rr = (r8 & 0xFF) + 4;
        int gr = (g8 & 0xFF) + 2;
        int br = (b8 & 0xFF) + 4;
        int r5 = (rr >>> 3) - (rr >>> 8);
        int g6 = (gr >>> 2) - (gr >>> 8);
        int b5 = (br >>> 3) - (br >>> 8);
        return pack565(r5, g6, b5);
    }

    // not ported.
    private static int pack565(int r5, int g6, int b5) {
        return ((r5 & 0x1F) << 11) | ((g6 & 0x3F) << 5) | (b5 & 0x1F);
    }

    // not ported.
    private static RGB16 getCached(int packed565) {
        return cache[packed565 & 0xFFFF];
    }


    /**
     * @param level direct brightness level: 0 -> black, 16 -> this (100%)
     * @return RGB16
     * not ported.
     */
    public RGB16 withBrightness(int level) {
        if (level < 1) return BLACK;
        if (level > 15) return this;

        int r = (r() * level) >> 4;
        int g = (g() * level) >> 4;
        int b = (b() * level) >> 4;
        return from(r, g, b);
    }

    /**
     * Native support extracted from InitLUT @0045225B native LUT shade-page semantics.
     * Fully ported replacement for g_pColorLUT_UNUSED_IN_JAVA page lookup.
     * this method MUST be used instead of native LUT lookup!
     */
    public RGB16 withShade(int page) {
        return withBrightness(16 - page);
    }

    @Override
    // not ported.
    public int r() {
        return ((((val >>> 11) & 0x1F) * 527) + 23) >>> 6;
    }

    @Override
    // not ported.
    public int g() {
        return ((((val >>> 5) & 0x3F) * 259) + 33) >>> 6;
    }

    @Override
    // not ported.
    public int b() {
        return (((val & 0x1F) * 527) + 23) >>> 6;
    }

    @Override
    // not ported.
    public int a() {
        return 0xFF;
    }


    // not ported.
    public RGB32 toRGB32() {
        return cache32[val & 0xFFFF];
    }

    // not ported.
    public int ARGB() {
        return RGB32.ARGB(r(), g(), b(), 0xFF);
    }

    // not ported.
    public int ABGR() {
        return RGB32.ABGR(r(), g(), b(), 0xFF);
    }

    @Override
    // not ported.
    public String toString() {
        return color24(r(), g(), b()) + '█';
    }

}
