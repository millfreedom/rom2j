package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.color.RGB16;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;
import ua.millfreedom.rom2.model.palette.Palettes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CBitmapFont extends CBaseFont {
    public final CSprite16 spr;

    /**
     * Java convenience overload for CBitmapFont::CBitmapFont @0045E39E default spacing.
     * not ported.
     */
    public CBitmapFont(String name)  {
        this(name, 2);
    }

    /**
     * Native: CBitmapFont::CBitmapFont @0045E39E.
     * Full port.
     */
    public CBitmapFont(String name, int spacing)  {
        this(new CSprite16(name + ".16"), loadGlyphWidths(name + ".dat"), spacing);
    }

    /**
     * Native support extracted from CBitmapFont::CBitmapFont @0045E39E resource loading.
     */
    private CBitmapFont(CSprite16 spr, int[] glyphWidths, int spacing) {
        super(spr, glyphWidths, spacing);
        this.spr = spr;
    }

    /**
     * Native: CBitmapFont::DrawTextShadowed @0045D536.
     * Full port.
     */
    public void drawTextShadowed(int x, int y, String text, int alignFlags, Palette16 textPalette, int shadowOffset) {
        drawTextInternal(x + shadowOffset, y + shadowOffset, text, alignFlags, getDefaultColorTable());
        drawTextInternal(x, y, text, alignFlags, textPalette);
    }

    /**
     * Native: CBitmapFont::drawWrappedJustifiedText @0045D58F.
     * Full port.
     */
    public void drawWrappedJustifiedText(CRect rect, String text, Palette16 textPalette, int lineSpacing) {
        int effectiveLineSpacing = lineSpacing == 0 ? getHeight() : lineSpacing;
        List<String> lines = formatText(rect, text);
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            if (rect.height() < lineIndex * effectiveLineSpacing + getHeight()) {
                break;
            }
            drawJustifiedLine(rect.left, rect.top + lineIndex * effectiveLineSpacing, rect.width(), lines.get(lineIndex), textPalette);
        }
    }

    /**
     * Native: CBitmapFont::drawWrappedJustifiedTextShadowed @0045D693.
     * Fully ported.
     */
    public void drawWrappedJustifiedTextShadowed(CRect rect, String text, Palette16 textPalette, int lineSpacing) {
        Objects.requireNonNull(rect, "rect");
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(textPalette, "textPalette");

        int effectiveLineSpacing = lineSpacing == 0 ? getHeight() : lineSpacing;
        List<String> lines = formatText(rect, text);
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            if (rect.height() < lineIndex * effectiveLineSpacing + getHeight()) {
                break;
            }
            drawJustifiedLineShadowed(rect.left, rect.top + lineIndex * effectiveLineSpacing, rect.width(),
                    lines.get(lineIndex), textPalette);
        }
    }

    /**
     * Native: CBitmapFont::drawTextRowsShadowed @0045D797.
     * Full port.
     */
    public void drawTextRowsShadowed(CRect rect, List<String> rows, Palette16 textPalette, int rowPitch, int firstRow, int endRow) {
        if (rowPitch == 0) {
            rowPitch = getHeight();
        }
        for (int rowIndex = firstRow; rowIndex < endRow; rowIndex++) {
            if (rect.height() < (rowIndex - firstRow) * rowPitch + getHeight()) {
                break;
            }
            drawTextShadowed(
                    rect.left,
                    rect.top + (rowIndex - firstRow) * rowPitch,
                    rows.get(rowIndex),
                    TextAlign.DEFAULT.mask,
                    textPalette,
                    1
            );
        }
    }

    /**
     * Native: CBitmapFont::drawWrappedTextRows @0045E01D.
     * Fully ported.
     */
    public void drawWrappedTextRows(CRect rect, int firstRow, int endRow, List<String> rows, Palette16 textPalette, int rowPitch) {
        Objects.requireNonNull(rect, "rect");
        Objects.requireNonNull(rows, "rows");
        Objects.requireNonNull(textPalette, "textPalette");

        if (rows.size() < endRow) {
            endRow = rows.size();
        }
        int effectiveRowPitch = rowPitch == 0 ? getHeight() : rowPitch;
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
                drawTextShadowed(drawX, drawY, drawText, TextAlign.DEFAULT.mask, textPalette, 1);
            } else if ((flags & 1) == 0) {
                drawJustifiedLineShadowed(rect.left, drawY, rect.width(), drawText, textPalette);
            } else {
                drawJustifiedLineShadowed(rect.left + paragraphIndent, drawY, rect.width() - paragraphIndent, drawText, textPalette);
            }
        }
    }

    /**
     * Native: CBitmapFont::drawJustifiedLineShadowed @0045DA67.
     * Fully ported.
     */
    public void drawJustifiedLineShadowed(int x, int y, int width, String text, Palette16 textPalette) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(textPalette, "textPalette");

        List<String> words = splitJustifiedWords(text);
        int totalTextWidth = 0;
        for (String word : words) {
            totalTextWidth += getTextWidth(word);
        }

        double extraSpace = words.size() < 2
                ? width - totalTextWidth
                : (double) (width - totalTextWidth) / (double) (words.size() - 1);
        double drawX = x;
        for (String word : words) {
            drawTextShadowed((int) drawX, y, word, TextAlign.DEFAULT.mask, textPalette, 1);
            drawX += getTextWidth(word) + extraSpace;
        }
    }

    /**
     * Native support extracted from CBitmapFont::drawJustifiedLineShadowed @0045DA67 CString word splitting.
     * Fully ported.
     */
    private static List<String> splitJustifiedWords(String text) {
        ArrayList<String> words = new ArrayList<>();
        String remaining = text.stripTrailing();
        do {
            remaining = remaining.stripLeading();
            int nextSpace = remaining.indexOf(' ');
            if (nextSpace < 0) {
                nextSpace = remaining.length();
            }
            String word = remaining.substring(0, nextSpace).stripTrailing();
            words.add(word);
            remaining = remaining.substring(nextSpace);
        } while (!remaining.isEmpty());
        return words;
    }

    /**
     * Native support extracted from CBitmapFont::drawWrappedTextRows @0045E01D paragraph checks.
     * Fully ported.
     */
    private static boolean endsWithParagraphMarker(String row) {
        return row.charAt(row.length() - 1) == '\r';
    }

    /**
     * Native support extracted from CBitmapFont::drawWrappedTextRows @0045E01D row cleanup.
     * Fully ported.
     */
    private static String stripParagraphMarker(String row) {
        return endsWithParagraphMarker(row) ? row.substring(0, row.length() - 1) : row;
    }

    /**
     * Native support extracted from CBitmapFont::DrawTextInternal @0045E531 byte glyph-frame locals.
     */
    private static int getDrawGlyphFrameIndex(byte charCode) {
        return (getGlyphIndex(charCode) - 0x20) & 0xFF;
    }

    /**
     * vtbl +0x14: CBitmapFont::DrawTextInternal @0045E531.
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
            y -= graphics.xSizeOf(0);
        }
        if (TextAlign.VERTICAL_CENTER.matches(alignFlags)) {
            y -= graphics.xSizeOf(0) / 2;
        }

        byte[] nativeText = getNativeTextBytes(text);
        for (int index = 0; index < nativeText.length; index++) {
            int currentFrameIndex = getDrawGlyphFrameIndex(nativeText[index]);
            int nextFrameIndex = getDrawGlyphFrameIndex(index + 1 < nativeText.length
                    ? nativeText[index + 1]
                    : (byte) 0);

            if (currentFrameIndex == 0x5E && nextFrameIndex != 0x5E) {
                int underlineY = y + getHeight();
                int underlineRight = x + glyphWidths[nextFrameIndex];
                RGB16 underlineColor = palette.data()[0x0F];
                Globals.renderer.drawLine(x, underlineY, underlineRight, underlineY, underlineColor.val());
            } else {
                if (currentFrameIndex == 0) {
                    x += getHeight() >> 1;
                } else {
                    spr.DrawFrameClippedY(x, y, currentFrameIndex, palette);
                }
                x += glyphWidths[currentFrameIndex] + spacing;
            }

            if (currentFrameIndex == 0x5E && nextFrameIndex == 0x5E) {
                index++;
            }
        }
    }

    /**
     * vtbl +0x18: CBitmapFont::GetDefaultColorTable @0045E746.
     * Full port. Native returns the shared flat palette table.
     */
    @Override
    public Palette16 getDefaultColorTable() {
        return Palettes.flat;
    }

    /**
     * Native: CBitmapFont::GetHeight @00402A90.
     * Fully ported.
     */
    public int getHeight() {
        return getFrameHeight();
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
