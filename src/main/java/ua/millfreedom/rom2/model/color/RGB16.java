package ua.millfreedom.rom2.model.color;

import static ua.millfreedom.rom2.console.Utils.color24;

/**
 * Stateless RGB565 conversion utilities. RGB565 values must not survive a resource-load boundary.
 */
public final class RGB16 {

    // not ported. Utility class.
    private RGB16() {
    }


    /**
     * not ported. Expands a packed RGB565 red channel to the full 0..255 range.
     */
    public static int r(int packed565) {
        int red5 = (packed565 >>> 11) & 0x1F;
        return expand5(red5);
    }

    /**
     * not ported. Expands a packed RGB565 green channel to the full 0..255 range.
     */
    public static int g(int packed565) {
        int green6 = (packed565 >>> 5) & 0x3F;
        return expand6(green6);
    }

    /**
     * not ported. Expands a packed RGB565 blue channel to the full 0..255 range.
     */
    public static int b(int packed565) {
        int blue5 = packed565 & 0x1F;
        return expand5(blue5);
    }

    /**
     * not ported. Expands one 5-bit RGB565 channel with the canonical full-range formula.
     */
    public static int expand5(int channel5) {
        return (((channel5 & 0x1F) * 527) + 23) >>> 6;
    }

    /**
     * not ported. Expands one 6-bit RGB565 channel with the canonical full-range formula.
     */
    public static int expand6(int channel6) {
        return (((channel6 & 0x3F) * 259) + 33) >>> 6;
    }

    /**
     * Native support extracted from Global::PackRoundedRgbToDisplayPixel @00474427.
     */
    public static short from(int red, int green, int blue) {
        int roundedRed = (red & 0xFF) + 4;
        int roundedGreen = (green & 0xFF) + 2;
        int roundedBlue = (blue & 0xFF) + 4;
        int red5 = (roundedRed >>> 3) - (roundedRed >>> 8);
        int green6 = (roundedGreen >>> 2) - (roundedGreen >>> 8);
        int blue5 = (roundedBlue >>> 3) - (roundedBlue >>> 8);
        return (short) (((red5 & 0x1F) << 11) | ((green6 & 0x3F) << 5) | (blue5 & 0x1F));
    }

}
