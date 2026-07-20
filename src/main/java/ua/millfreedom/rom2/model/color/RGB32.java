package ua.millfreedom.rom2.model.color;

import static ua.millfreedom.rom2.console.Utils.color24;

/**
 * Stateless plain-int ARGB utilities.
 */
public final class RGB32 {
    public static final int BLACK = 0xFF00_0000;
    public static final int WHITE = 0xFFFF_FFFF;
    public static final int RED = 0xFFFF_0000;
    public static final int GREEN = 0xFF00_FF00;
    public static final int BLUE = 0xFF00_00FF;
    public static final int TBLACK = 0x0000_0000;

    // not ported. Utility class.
    private RGB32() {
    }

    // not ported.
    public static int from(int red, int green, int blue, int alpha) {
        return ARGB(red, green, blue, alpha);
    }

    // not ported.
    public static int from(int red, int green, int blue) {
        return ARGB(red, green, blue, 0xFF);
    }

    /**
     * not ported. Forces opaque alpha while preserving the low RGB24 bits.
     */
    public static int opaque(int rgb24) {
        return BLACK | (rgb24 & 0x00FF_FFFF);
    }


    // not ported.
    public static int ARGB(int red, int green, int blue, int alpha) {
        return (alpha & 0xFF) << 24
                | (red & 0xFF) << 16
                | (green & 0xFF) << 8
                | (blue & 0xFF);
    }

    // not ported.
    public static int ABGR(int red, int green, int blue, int alpha) {
        return (alpha & 0xFF) << 24
                | (blue & 0xFF) << 16
                | (green & 0xFF) << 8
                | (red & 0xFF);
    }

    // not ported.
    public static int toARGB(int color) {
        return color;
    }

    // not ported.
    public static int toABGR(int color) {
        return a(color) << 24 | b(color) << 16 | g(color) << 8 | r(color);
    }

    /**
     * not ported. Applies native 0..16 brightness with packed channel multiplication.
     */
    public static int withBrightness(int color, int level) {
        int redBlue = ((color & 0x00FF_00FF) * level) >> 4;
        int green = ((color & 0x0000_FF00) * level) >> 4;
        return (color & 0xFF00_0000) | (redBlue & 0x00FF_00FF) | (green & 0x0000_FF00);
    }

    /**
     * Native support extracted from InitLUT @0045225B native LUT shade-page semantics.
     */
    public static int withShade(int color, int page) {
        return withBrightness(color, 16 - page);
    }

    /**
     * not ported. Replaces a straight-ARGB color's alpha channel.
     */
    public static int withAlpha(int color, int alpha) {
        return (Utils.clamp255(alpha) << 24) | (color & 0x00FF_FFFF);
    }

    /**
     * not ported. Scales a straight-ARGB color's alpha by an additional 0..255 opacity.
     */
    public static int scaleAlpha(int color, int opacity) {
        int scaledAlpha = (a(color) * Utils.clamp255(opacity) + 0x7F) / 0xFF;
        return withAlpha(color, scaledAlpha);
    }

    /**
     * not ported. Applies the native fixed-half blend for opaque source and destination pixels.
     */
    public static int blendHalfOpaque(int source, int destination) {
        return ARGB(
                (r(source) * 0x80 + r(destination) * 0x7F + 0x7F) / 0xFF,
                (g(source) * 0x80 + g(destination) * 0x7F + 0x7F) / 0xFF,
                (b(source) * 0x80 + b(destination) * 0x7F + 0x7F) / 0xFF,
                0xFF
        );
    }

    /**
     * not ported. Composites straight-ARGB source over straight-ARGB destination.
     */
    public static int sourceOver(int source, int destination) {
        int sourceAlpha = a(source);
        if (sourceAlpha == 0) {
            return destination;
        }
        if (sourceAlpha == 0xFF) {
            return source;
        }

        int destinationAlpha = a(destination);
        if (destinationAlpha == 0) {
            return source;
        }

        int inverseSourceAlpha = 0xFF - sourceAlpha;
        int outputAlphaNumerator = sourceAlpha * 0xFF + destinationAlpha * inverseSourceAlpha;
        int outputAlpha = (outputAlphaNumerator + 0x7F) / 0xFF;
        int rounding = outputAlphaNumerator >>> 1;
        int red = (r(source) * sourceAlpha * 0xFF
                + r(destination) * destinationAlpha * inverseSourceAlpha
                + rounding) / outputAlphaNumerator;
        int green = (g(source) * sourceAlpha * 0xFF
                + g(destination) * destinationAlpha * inverseSourceAlpha
                + rounding) / outputAlphaNumerator;
        int blue = (b(source) * sourceAlpha * 0xFF
                + b(destination) * destinationAlpha * inverseSourceAlpha
                + rounding) / outputAlphaNumerator;
        return ARGB(red, green, blue, outputAlpha);
    }

    /**
     * not ported. Adds straight-ARGB source light over destination while carrying source-over alpha.
     */
    public static int additiveOver(int source, int destination) {
        int sourceAlpha = a(source);
        if (sourceAlpha == 0) {
            return destination;
        }

        int destinationAlpha = a(destination);
        int inverseSourceAlpha = 0xFF - sourceAlpha;
        int outputAlpha = sourceAlpha + (destinationAlpha * inverseSourceAlpha + 0x7F) / 0xFF;
        int redPremultiplied = Math.min(outputAlpha * 0xFF,
                r(source) * sourceAlpha + r(destination) * destinationAlpha);
        int greenPremultiplied = Math.min(outputAlpha * 0xFF,
                g(source) * sourceAlpha + g(destination) * destinationAlpha);
        int bluePremultiplied = Math.min(outputAlpha * 0xFF,
                b(source) * sourceAlpha + b(destination) * destinationAlpha);
        return ARGB(
                (redPremultiplied + (outputAlpha >>> 1)) / outputAlpha,
                (greenPremultiplied + (outputAlpha >>> 1)) / outputAlpha,
                (bluePremultiplied + (outputAlpha >>> 1)) / outputAlpha,
                outputAlpha
        );
    }

    // not ported.
    public static int r(int color) {
        return (color >>> 16) & 0xFF;
    }

    // not ported.
    public static int g(int color) {
        return (color >>> 8) & 0xFF;
    }

    // not ported.
    public static int b(int color) {
        return color & 0xFF;
    }

    // not ported.
    public static int a(int color) {
        return (color >>> 24) & 0xFF;
    }

    // not ported.
    public static String toString(int color) {
        return color24(r(color), g(color), b(color)) + '█';
    }
}
