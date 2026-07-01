package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

/**
 * Java-only tooltip text color palette codes.
 * not ported.
 */
public enum TooltipColor {
    SEPIA((char) 0x01, (char) 0x11, Palettes.sepia),
    GRAY((char) 0x02, (char) 0x12, Palettes.gray),
    GRAY_DIM((char) 0x03, (char) 0x13, Palettes.grayDim),
    YELLOWISH((char) 0x04, (char) 0x14, Palettes.yellowish),
    ORANGEISH((char) 0x05, (char) 0x15, Palettes.orangeish),
    REDISH((char) 0x06, (char) 0x16, Palettes.redish),
    GREENISH((char) 0x07, (char) 0x17, Palettes.greenish),
    DARK_RED((char) 0x08, (char) 0x18, Palettes.darkRed),
    BROWNISH((char) 0x0B, (char) 0x19, Palettes.brownish),
    HIGHLIGHT((char) 0x0C, (char) 0x1A, Palettes.highlight),
    HOVER((char) 0x0E, (char) 0x1B, Palettes.hover);

    private static final char SPAN_CLOSE_CODE = (char) 0x1D;

    private final char nextCode;
    private final char spanOpenCode;
    private final Palette16 palette;

    /**
     * Java-only enum storage for tooltip color control codes.
     * not ported.
     */
    TooltipColor(char nextCode, char spanOpenCode, Palette16 palette) {
        this.nextCode = nextCode;
        this.spanOpenCode = spanOpenCode;
        this.palette = palette;
    }

    /**
     * Java-only control code that colors the next visible tooltip character.
     * not ported.
     */
    public char nextCode() {
        return nextCode;
    }

    /**
     * Java-only control code that opens a tooltip color span.
     * not ported.
     */
    public char spanOpenCode() {
        return spanOpenCode;
    }

    /**
     * Java-only tooltip palette resolved by this color code.
     * not ported.
     */
    public Palette16 palette() {
        return palette;
    }

    /**
     * Java-only shared control code that closes the current tooltip color span.
     * not ported.
     */
    public static char spanCloseCode() {
        return SPAN_CLOSE_CODE;
    }

    /**
     * Java-only lookup for next-character tooltip color codes.
     * not ported.
     */
    public static TooltipColor forNextCode(char code) {
        for (TooltipColor color : values()) {
            if (color.nextCode == code) {
                return color;
            }
        }
        return null;
    }

    /**
     * Java-only lookup for open-span tooltip color codes.
     * not ported.
     */
    public static TooltipColor forSpanOpenCode(char code) {
        for (TooltipColor color : values()) {
            if (color.spanOpenCode == code) {
                return color;
            }
        }
        return null;
    }
}
