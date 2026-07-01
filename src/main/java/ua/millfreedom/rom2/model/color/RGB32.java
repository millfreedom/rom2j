package ua.millfreedom.rom2.model.color;

import static ua.millfreedom.rom2.console.Utils.color24;
import static ua.millfreedom.rom2.model.color.Utils.clamp255;

public record RGB32(int val) implements RGBA {

    public static RGB32 BLACK = RGB32.from(0, 0, 0);
    public static RGB32 WHITE = RGB32.from(255, 255, 255);
    public static RGB32 RED = RGB32.from(255, 0, 0);
    public static RGB32 GREEN = RGB32.from(0, 255, 0);
    public static RGB32 BLUE = RGB32.from(0, 0, 255);
    public static RGB32 TBLACK = RGB32.from(0, 0, 0, 0);

    // not ported.
    public static RGB32 from(int r, int g, int b, int a) {
        return new RGB32(ARGB(r, g, b, a));
    }

    // not ported.
    public static RGB32 from(int r, int g, int b) {
        return from(r, g, b, 0xFF);
    }

    // not ported.
    public RGB16 toRGB16() {
        return RGB16.from(r(), g(), b());
    }

    // not ported.
    public static int ARGB(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    // not ported.
    public static int ABGR(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (b & 0xFF) << 16 | (g & 0xFF) << 8 | (r & 0xFF);
    }

    // not ported.
    public int toARGB() {
        return val;
    }

    // not ported.
    public int toABGR() {
        return a() << 24 | b() << 16 | g() << 8 | r();
    }

    /**
     * @param level direct brightness level: 0 -> black, 16 -> this (100%)
     * @return RGB32
     * not ported.
     */
    public RGB32 withBrightness(int level) {
        if (level < 1) return BLACK;
        if (level > 15) return this;
        int r = clamp255((r() * level) >> 4);
        int g = clamp255((g() * level) >> 4);
        int b = clamp255((b() * level) >> 4);
        return from(r, g, b);
    }

    /**
     * Native support extracted from InitLUT @0045225B native LUT shade-page semantics.
     * Fully ported expanded-32bpp replacement for g_pColorLUT_UNUSED_IN_JAVA page lookup.
     */
    public RGB32 withShade(int page) {
        return withBrightness(16 - page);
    }

    @Override
    // not ported.
    public int r() {
        return (val >>> 16) & 0xFF;
    }

    @Override
    // not ported.
    public int g() {
        return (val >>> 8) & 0xFF;
    }

    @Override
    // not ported.
    public int b() {
        return val & 0xFF;
    }

    @Override
    // not ported.
    public int a() {
        return (val >>> 24) & 0xFF;
    }

    @Override
    // not ported.
    public String toString() {
        return color24(r(), g(), b()) + '█';
    }
}
