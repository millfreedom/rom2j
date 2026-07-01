package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java-only tooltip text composition helpers.
 * not ported.
 */
public final class TooltipText {
    private static final String ROW_DELIMITER = "#";
    private static final String SIDE_BY_SIDE_PREFIX = "\u001E";
    private static final String SIDE_BY_SIDE_SEPARATOR = "\u001F";
    private static final char RGB_NEXT_COLOR_CODE = (char) 0x0F;
    private static final char RGB_SPAN_OPEN_COLOR_CODE = (char) 0x10;
    private static final int RGB_PAYLOAD_HEX_LENGTH = 6;
    private static final int RGB_PALETTE_SIZE = 16;
    private static final Map<RGB32, Palette16> RGB_PALETTE_CACHE = new HashMap<>();

    /**
     * Java-only utility class guard.
     * not ported.
     */
    private TooltipText() {
    }

    /**
     * Java-only formatter that asks the tooltip renderer to draw two normal tooltips side-by-side.
     * not ported.
     */
    public static String sideBySide(String leftTooltip, String rightTooltip) {
        return SIDE_BY_SIDE_PREFIX + leftTooltip + SIDE_BY_SIDE_SEPARATOR
                + TooltipText.colorSpan(RGB32.GREEN, "Equipped") + ROW_DELIMITER + rightTooltip;
    }

    /**
     * Java-only detector for the side-by-side tooltip renderer payload.
     * not ported.
     */
    public static boolean isSideBySide(String tooltip) {
        return tooltip.startsWith(SIDE_BY_SIDE_PREFIX) && tooltip.indexOf(SIDE_BY_SIDE_SEPARATOR, SIDE_BY_SIDE_PREFIX.length()) >= 0;
    }

    /**
     * Java-only splitter for a side-by-side tooltip renderer payload.
     * not ported.
     */
    public static String[] splitSideBySide(String tooltip) {
        int separatorIndex = tooltip.indexOf(SIDE_BY_SIDE_SEPARATOR, SIDE_BY_SIDE_PREFIX.length());
        if (separatorIndex < 0 || !tooltip.startsWith(SIDE_BY_SIDE_PREFIX)) {
            throw new IllegalArgumentException("Not a side-by-side tooltip");
        }
        return new String[]{
                tooltip.substring(SIDE_BY_SIDE_PREFIX.length(), separatorIndex),
                tooltip.substring(separatorIndex + SIDE_BY_SIDE_SEPARATOR.length())
        };
    }

    /**
     * Java-only helper preserving CMousePointer's native `#` tooltip row delimiter contract.
     * not ported.
     */
    public static String[] splitRows(String tooltip) {
        return tooltip.split(ROW_DELIMITER, -1);
    }

    /**
     * Java-only formatter for coloring the next visible tooltip character.
     * not ported.
     */
    public static String colorNext(TooltipColor color, char character) {
        return String.valueOf(color.nextCode()) + character;
    }

    /**
     * Java-only formatter for prefixing text with a next-visible-character tooltip color code.
     * not ported.
     */
    public static String colorNext(TooltipColor color, String text) {
        if (text.isEmpty()) {
            return text;
        }
        return color.nextCode() + text;
    }

    /**
     * Java-only formatter for coloring the next visible tooltip character with a generated RGB palette.
     * not ported.
     */
    public static String colorNext(RGB32 color, char character) {
        return encodeRgbControl(RGB_NEXT_COLOR_CODE, color) + character;
    }

    /**
     * Java-only formatter for prefixing text with a next-visible-character generated RGB tooltip color code.
     * not ported.
     */
    public static String colorNext(RGB32 color, String text) {
        if (text.isEmpty()) {
            return text;
        }
        return encodeRgbControl(RGB_NEXT_COLOR_CODE, color) + text;
    }

    /**
     * Java-only formatter for wrapping tooltip text in an open/close color span.
     * not ported.
     */
    public static String colorSpan(TooltipColor color, String text) {
        return openColor(color) + text + closeColor();
    }

    /**
     * Java-only formatter for wrapping tooltip text in a generated RGB color span.
     * not ported.
     */
    public static String colorSpan(RGB32 color, String text) {
        return openColor(color) + text + closeColor();
    }

    /**
     * Java-only formatter for opening a tooltip color span.
     * not ported.
     */
    public static String openColor(TooltipColor color) {
        return String.valueOf(color.spanOpenCode());
    }

    /**
     * Java-only formatter for opening a generated RGB tooltip color span.
     * not ported.
     */
    public static String openColor(RGB32 color) {
        return encodeRgbControl(RGB_SPAN_OPEN_COLOR_CODE, color);
    }

    /**
     * Java-only formatter for closing the current tooltip color span.
     * not ported.
     */
    public static String closeColor() {
        return String.valueOf(TooltipColor.spanCloseCode());
    }

    /**
     * Java-only detector for tooltip color control codes.
     * not ported.
     */
    public static boolean isColorControlCode(char code) {
        return TooltipColor.forNextCode(code) != null
                || TooltipColor.forSpanOpenCode(code) != null
                || code == RGB_NEXT_COLOR_CODE
                || code == RGB_SPAN_OPEN_COLOR_CODE
                || code == TooltipColor.spanCloseCode();
    }

    /**
     * Java-only helper that removes tooltip color controls before font measurement.
     * not ported.
     */
    public static String stripColorCodes(String text) {
        StringBuilder visibleText = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char code = text.charAt(i);
            if (isRgbColorControlCode(code)) {
                parseRgbPayload(text, i);
                i += RGB_PAYLOAD_HEX_LENGTH;
                continue;
            }
            if (!isColorControlCode(code)) {
                visibleText.append(code);
            }
        }
        return visibleText.toString();
    }

    /**
     * Java-only parser that converts tooltip color controls into draw-ready palette runs.
     * not ported.
     */
    public static List<ColoredRun> coloredRuns(String text, Palette16 defaultPalette) {
        ArrayList<ColoredRun> runs = new ArrayList<>();
        ArrayDeque<Palette16> spanPaletteStack = new ArrayDeque<>();
        StringBuilder runText = new StringBuilder(text.length());
        Palette16 currentPalette = defaultPalette;
        Palette16 nextPalette = null;

        for (int i = 0; i < text.length(); i++) {
            char code = text.charAt(i);
            TooltipColor nextColor = TooltipColor.forNextCode(code);
            if (nextColor != null) {
                appendRun(runs, runText, currentPalette);
                nextPalette = nextColor.palette();
                continue;
            }

            if (code == RGB_NEXT_COLOR_CODE) {
                appendRun(runs, runText, currentPalette);
                nextPalette = rgbPalette(parseRgbPayload(text, i));
                i += RGB_PAYLOAD_HEX_LENGTH;
                continue;
            }

            TooltipColor spanColor = TooltipColor.forSpanOpenCode(code);
            if (spanColor != null) {
                appendRun(runs, runText, currentPalette);
                spanPaletteStack.push(currentPalette);
                currentPalette = spanColor.palette();
                continue;
            }

            if (code == RGB_SPAN_OPEN_COLOR_CODE) {
                appendRun(runs, runText, currentPalette);
                spanPaletteStack.push(currentPalette);
                currentPalette = rgbPalette(parseRgbPayload(text, i));
                i += RGB_PAYLOAD_HEX_LENGTH;
                continue;
            }

            if (code == TooltipColor.spanCloseCode()) {
                appendRun(runs, runText, currentPalette);
                currentPalette = spanPaletteStack.isEmpty() ? defaultPalette : spanPaletteStack.pop();
                continue;
            }

            if (nextPalette != null) {
                appendRun(runs, runText, currentPalette);
                runs.add(new ColoredRun(String.valueOf(code), nextPalette));
                nextPalette = null;
                continue;
            }

            runText.append(code);
        }
        appendRun(runs, runText, currentPalette);
        return runs;
    }

    /**
     * Java-only detector for RGB tooltip color controls.
     * not ported.
     */
    private static boolean isRgbColorControlCode(char code) {
        return code == RGB_NEXT_COLOR_CODE || code == RGB_SPAN_OPEN_COLOR_CODE;
    }

    /**
     * Java-only formatter for delimiter-safe RGB tooltip control payloads.
     * not ported.
     */
    private static String encodeRgbControl(char controlCode, RGB32 color) {
        StringBuilder builder = new StringBuilder(1 + RGB_PAYLOAD_HEX_LENGTH);
        builder.append(controlCode);
        appendHexByte(builder, color.r());
        appendHexByte(builder, color.g());
        appendHexByte(builder, color.b());
        return builder.toString();
    }

    /**
     * Java-only formatter for one RGB payload byte.
     * not ported.
     */
    private static void appendHexByte(StringBuilder builder, int value) {
        builder.append(hexDigit((value >>> 4) & 0x0F));
        builder.append(hexDigit(value & 0x0F));
    }

    /**
     * Java-only formatter for one RGB payload hex digit.
     * not ported.
     */
    private static char hexDigit(int value) {
        return (char) (value < 10 ? '0' + value : 'A' + value - 10);
    }

    /**
     * Java-only parser for an RGB tooltip color control payload.
     * not ported.
     */
    private static RGB32 parseRgbPayload(String text, int controlIndex) {
        if (controlIndex + RGB_PAYLOAD_HEX_LENGTH >= text.length()) {
            throw new IllegalArgumentException("Malformed RGB tooltip color control");
        }
        int red = parseHexByte(text, controlIndex + 1);
        int green = parseHexByte(text, controlIndex + 3);
        int blue = parseHexByte(text, controlIndex + 5);
        return RGB32.from(red, green, blue);
    }

    /**
     * Java-only parser for one RGB tooltip payload byte.
     * not ported.
     */
    private static int parseHexByte(String text, int index) {
        return (parseHexDigit(text.charAt(index)) << 4) | parseHexDigit(text.charAt(index + 1));
    }

    /**
     * Java-only parser for one RGB tooltip payload hex digit.
     * not ported.
     */
    private static int parseHexDigit(char digit) {
        if ('0' <= digit && digit <= '9') {
            return digit - '0';
        }
        if ('A' <= digit && digit <= 'F') {
            return digit - 'A' + 10;
        }
        if ('a' <= digit && digit <= 'f') {
            return digit - 'a' + 10;
        }
        throw new IllegalArgumentException("Malformed RGB tooltip color control");
    }

    /**
     * Java-only cache lookup for generated RGB tooltip palettes.
     * not ported.
     */
    private static Palette16 rgbPalette(RGB32 color) {
        return RGB_PALETTE_CACHE.computeIfAbsent(color, TooltipText::buildRgbPalette);
    }

    /**
     * Java-only generator for a 16-entry black-to-RGB tooltip palette ramp.
     * not ported.
     */
    private static Palette16 buildRgbPalette(RGB32 color) {
        RGB16[] colors = new RGB16[RGB_PALETTE_SIZE];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = rgbPaletteStep(color, i);
        }
        return new Palette16(colors);
    }

    /**
     * Java-only generator for one black-to-RGB tooltip palette ramp step.
     * not ported.
     */
    private static RGB16 rgbPaletteStep(RGB32 color, int step) {
        return RGB16.from(
                (color.r() * step) / 0x0F,
                (color.g() * step) / 0x0F,
                (color.b() * step) / 0x0F
        );
    }

    /**
     * Java-only helper for appending a parsed tooltip color run.
     * not ported.
     */
    private static void appendRun(List<ColoredRun> runs, StringBuilder runText, Palette16 palette) {
        if (runText.length() == 0) {
            return;
        }
        runs.add(new ColoredRun(runText.toString(), palette));
        runText.setLength(0);
    }

    /**
     * Java-only parsed tooltip text run.
     * not ported.
     */
    public static final class ColoredRun {
        public final String text;
        public final Palette16 palette;

        /**
         * Java-only holder for a visible tooltip text run and its palette.
         * not ported.
         */
        private ColoredRun(String text, Palette16 palette) {
            this.text = text;
            this.palette = palette;
        }
    }
}
