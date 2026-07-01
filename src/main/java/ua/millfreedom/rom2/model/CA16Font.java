package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CA16Font extends CBaseFont {
    public final CA16 spr;

    /**
     * Java convenience overload for CA16Font::CA16Font @0045E756 default spacing.
     * not ported.
     */
    public CA16Font(String name) throws Exception {
        this(name, 2);
    }

    /**
     * Native: CA16Font::CA16Font @0045E756.
     * Full port.
     */
    public CA16Font(String name, int spacing) {
        this(new CA16(name + ".16a"), loadGlyphWidths(name + ".dat"), spacing);
    }

    /**
     * Native support extracted from CA16Font::CA16Font @0045E756 resource loading and palette initialization.
     */
    private CA16Font(CA16 spr, int[] glyphWidths, int spacing) {
        super(spr, glyphWidths, spacing);
        this.spr = spr;
        spr.initPalette(0x10, 4, 0);
    }

    /**
     * Native support extracted from CBitmapFont::DrawTextShadowed @0045D536 for CA16Font virtual text dispatch.
     */
    public void drawTextShadowed(int x, int y, String text, int alignFlags, Palette16 textPalette, int shadowOffset) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(textPalette, "textPalette");

        drawTextInternal(x + shadowOffset, y + shadowOffset, text, alignFlags, getDefaultColorTable());
        drawTextInternal(x, y, text, alignFlags, textPalette);
    }

    /**
     * vtbl +0x14: CA16Font::DrawTextInternal @0045E8FD.
     * Full port.
     */
    @Override
    public void drawTextInternal(int x, int y, String text, int alignFlags, Palette16 palette) {
        if (TextAlign.RIGHT.matches(alignFlags)) {
            x -= getTextWidth(text);
        }
        if (TextAlign.CENTER.matches(alignFlags)) {
            x -= getTextWidth(text) / 2;
        }
        if (TextAlign.BOTTOM.matches(alignFlags)) {
            y -= getFrameHeight();
        }
        if (TextAlign.VERTICAL_CENTER.matches(alignFlags)) {
            y -= getFrameHeight() / 2;
        }

        Object paletteOverride = Palettes.a16FontPaletteOverride(palette);
        for (int index = 0; index < text.length(); index++) {
            int currentFrameIndex = getGlyphFrameIndex((byte) text.charAt(index));
            if (currentFrameIndex == 0) {
                x += getFrameHeight() >> 1;
            } else {
                spr.draw(x, y, currentFrameIndex, paletteOverride, false);
            }
            x += glyphWidths[currentFrameIndex] + spacing;
        }
    }

    /**
     * Native support: CA16Font wrapped-row helper at @0045DC9A, used by imageContainer::DrawContent @004A8841.
     * Full port.
     */
    public void drawWrappedTextRows(CRect rect, int firstRow, int endRow, List<String> rows, Palette16 textPalette, int rowPitch) {
        if (rows.size() < endRow) {
            endRow = rows.size();
        }
        int effectiveRowPitch = rowPitch == 0 ? getFrameHeight() : rowPitch;
        int paragraphIndent = glyphWidths[0x80];
        for (int rowIndex = firstRow; rowIndex < endRow; rowIndex++) {
            int flags = 0;
            String row = rows.get(rowIndex);
            if (rowIndex != rows.size() - 1 && !endsWithParagraphMarker(row)) {
                flags |= 2;
            }
            if (rowIndex == 0 || endsWithParagraphMarker(rows.get(rowIndex - 1))) {
                flags |= 1;
                if (getTextWidth(row + ' ') > rect.width()) {
                    flags |= 2;
                }
            }
            if (rowIndex == rows.size() - 1 || !endsWithParagraphMarker(row)) {
                flags |= 4;
            }

            String drawText = stripParagraphMarker(row);
            int drawY = rect.top + (rowIndex - firstRow) * effectiveRowPitch;
            if ((flags & 2) == 0) {
                int drawX = rect.left + (((flags & 1) == 0) ? 0 : paragraphIndent);
                drawTextInternal(drawX, drawY, drawText, TextAlign.DEFAULT.mask, textPalette);
            } else if ((flags & 1) == 0) {
                drawJustifiedLine(rect.left, drawY, rect.width(), drawText, textPalette);
            } else {
                drawJustifiedLine(rect.left + paragraphIndent, drawY, rect.width() - paragraphIndent, drawText, textPalette);
            }
        }
    }

    /**
     * Native support extracted from CA16Font wrapped-row helper @0045DC9A paragraph checks.
     * Full port.
     */
    private static boolean endsWithParagraphMarker(String row) {
        return row.charAt(row.length() - 1) == '\r';
    }

    /**
     * Native support extracted from CA16Font wrapped-row helper @0045DC9A row cleanup.
     * Full port.
     */
    private static String stripParagraphMarker(String row) {
        return endsWithParagraphMarker(row) ? row.substring(0, row.length() - 1) : row;
    }

    /**
     * vtbl +0x18: CA16Font::GetDefaultColorTable @0045EA58.
     * Full port. Native returns palette page `0` from `g_pPalette3`.
     */
    @Override
    public Palette16 getDefaultColorTable() {
        return Palettes.p3.paletteData[0];
    }

    /**
     * not ported.
     */
    @Override
    public String toString() {
        return "CBitmapFont{" +
                "spacing=" + spacing +
                ", spr=" + spr +
                ", glyphWidths=" + Arrays.toString(glyphWidths) +
                '}';
    }
}
