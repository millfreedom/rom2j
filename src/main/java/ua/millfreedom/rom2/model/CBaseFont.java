package ua.millfreedom.rom2.model;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.TextAlign;
import ua.millfreedom.rom2.model.palette.Palette16;

import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ua.millfreedom.rom2.Globals.gameFileManager;

public class CBaseFont implements MfcSerializable {
    // Java support, not a native field.
    protected static final Charset NATIVE_FONT_CHARSET = Charset.forName("Cp866");
    //0x04
    public final CGameBitmap graphics;
    //0x08
    public final int[] glyphWidths;
    //0x0c
    public final int spacing;

    /**
     * Native support for CBaseFont fields initialized by CBaseFont::New @0045CA30 and derived font constructors.
     */
    protected CBaseFont(CGameBitmap graphics, int[] glyphWidths, int spacing) {
        this.graphics = graphics;
        this.glyphWidths = glyphWidths;
        this.spacing = spacing;
    }

    /**
     * vtbl +0x08: CObject::Serialize @00401970.
     */
    @Override
    public void serialize(CArchive ar) {
        // Native CObject::Serialize is a no-op.
    }

    /**
     * Native support: CBitmapFont::CBitmapFont @0045E39E and CA16Font constructor resource loads.
     */
    @SneakyThrows
    protected static int[] loadGlyphWidths(String name) {
        IntBuffer intBuffer = gameFileManager.get(name).asIntBuffer();
        int[] result = new int[intBuffer.limit()];
        intBuffer.get(result);
        return result;

    }


    /**
     * Native: CBaseFont::GetGlyphIndex @0045CAE1.
     * Full port.
     */
    public static int getGlyphIndex(byte charCode) {
        int c = charCode & 0xFF;
        if (Globals.useCustomEncoding) {
            if ((c < 0x80) || (0xAF < c)) {
                if ((0xDF < c) && (c < 0xF0)) {
                    c += 0x10;
                }
            } else {
                c += 0x30;
            }
        }
        return c;
    }

    /**
     * Native: CBaseFont::GetTextWidth @0045CB59.
     * Full port.
     */
    public int getTextWidth(String text) {
        int width = 0;
        byte[] nativeText = getNativeTextBytes(text);
        for (int index = 0; index < nativeText.length; index++) {
            int rawChar = nativeText[index] & 0xFF;
            if (rawChar <= 0x1F) {
                continue;
            }

            if (rawChar == '~') {
                boolean hasEscapedTilde = index + 1 < nativeText.length && nativeText[index + 1] == '~';
                if (!hasEscapedTilde) {
                    continue;
                }
                index++;
            }

            int glyphFrameIndex = getGlyphFrameIndex((byte) rawChar);
            width += glyphWidths[glyphFrameIndex] + spacing;
            if (glyphFrameIndex == 0) {
                width += getFrameHeight() >> 1;
            }
        }
        return width;
    }

    /**
     * vtbl +0x14: CBaseFont::DrawTextInternal @0045F8B0.
     * Full port. Native base implementation is a no-op.
     */
    public void drawTextInternal(int x, int y, String text, int alignFlags, Palette16 palette) {
    }

    /**
     * Java support method, not ported
     */
    protected static byte @NotNull [] getNativeTextBytes(String text) {
        return text.getBytes(NATIVE_FONT_CHARSET);
    }

    /**
     * vtbl +0x18: CBaseFont::GetDefaultColorTable @0045F8C0.
     * Full port. Native base implementation returns null.
     */
    public Palette16 getDefaultColorTable() {
        return null;
    }

    /**
     * Native: CBaseFont::drawJustifiedLine @0045D833.
     * Full port.
     */
    public void drawJustifiedLine(int x, int y, int width, String text, Palette16 textPalette) {
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
            drawTextInternal((int) drawX, y, word, TextAlign.DEFAULT.mask, textPalette);
            drawX += getTextWidth(word) + extraSpace;
        }
    }

    /**
     * Native: CBaseFont::justifyTextLine @0045CF9F.
     * Full port.
     */
    public String justifyTextLine(String text, int width) {
        String remaining = text.stripLeading().stripTrailing();
        int spaceWidth = getTextWidth(" ");
        int distributedSpaceCount = (width - getTextWidth(remaining)) / spaceWidth;
        int gapCount = 0;
        for (int index = 0; index < remaining.length(); index++) {
            if (remaining.charAt(index) == ' ') {
                gapCount++;
            }
        }

        distributedSpaceCount += gapCount;
        double spacesPerGap = 0.0;
        if (gapCount != 0) {
            spacesPerGap = (double) distributedSpaceCount / (double) gapCount;
        }

        int nativeLoopGate = gapCount + 1;
        if (nativeLoopGate <= 1) {
            return remaining;
        }

        StringBuilder justified = new StringBuilder();
        double accumulatedSpaces = 0.0;
        int emittedSpaces = 0;
        String spaceRun = "";
        do {
            accumulatedSpaces += spacesPerGap;
            int spacesForGap = (int) ((accumulatedSpaces - (double) emittedSpaces) + 0.5);
            emittedSpaces += spacesForGap;
            if (spacesForGap != 0) {
                spaceRun = " ".repeat(spacesForGap);
            }

            remaining = remaining.stripLeading();
            int wordLength = remaining.indexOf(' ');
            if (wordLength < 0) {
                wordLength = remaining.length();
            }
            justified.append(remaining, 0, wordLength);
            justified.append(spaceRun);
            remaining = remaining.substring(wordLength);
        } while (!remaining.isEmpty());
        return justified.toString().stripTrailing();
    }

    /**
     * Native support extracted from CBaseFont::drawJustifiedLine @0045D833 CString word splitting.
     * Full port.
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
     * Native: CBaseFont::FitIntoRect @0045CCBB.
     * Ported with a no-progress guard for exact-width unspaced text.
     */
    public List<String> fitIntoRect(CRect rect, String text) {
        Objects.requireNonNull(rect, "rect");
        Objects.requireNonNull(text, "text");

        ArrayList<String> lines = new ArrayList<>();
        int maxWidth = rect.width();
        String remaining = text.stripLeading().stripTrailing();
        while (!remaining.isEmpty()) {
            int committedLength = 0;
            int candidateLength = 0;
            String remainingTail = remaining;
            String candidateLine = "";

            while (true) {
                if (getTextWidth(remaining) < maxWidth) {
                    remaining += '\r';
                    committedLength = remaining.length();
                    break;
                }

                committedLength = candidateLength;
                int nextSpaceIndex = remainingTail.indexOf(' ');
                if (nextSpaceIndex < 0) {
                    candidateLength = remaining.length();
                } else {
                    candidateLength += nextSpaceIndex + 1;
                }

                candidateLine = remaining.substring(0, candidateLength);
                remainingTail = remaining.substring(candidateLength);
                if (remainingTail.isEmpty() && getTextWidth(candidateLine) < maxWidth) {
                    committedLength = candidateLength;
                    break;
                }
                if (getTextWidth(candidateLine) >= maxWidth) {
                    break;
                }
            }

            if (maxWidth <= getTextWidth(candidateLine) && committedLength == 0) {
                committedLength = candidateLength;
            }

            String committedLine = remaining.substring(0, committedLength).stripLeading();
            lines.add(committedLine);
            remaining = remaining.substring(committedLength).stripLeading();
        }
        return lines;
    }

    /**
     * Native: CBaseFont::FormatText @0045D479.
     * Fully ported.
     */
    public List<String> formatText(CRect rect, String text) {
        Objects.requireNonNull(rect, "rect");
        Objects.requireNonNull(text, "text");

        ArrayList<String> lines = new ArrayList<>();
        for (String paragraph : splitCrLfParagraphs(text)) {
            lines.addAll(fitIntoRect(rect, paragraph));
        }
        return lines;
    }

    /**
     * Java helper for native glyph-width array addressing in CBaseFont text metrics.
     * not ported.
     */
    protected final int getGlyphFrameIndex(byte charCode) {
        return (getGlyphIndex(charCode) & 0xFF) - 0x20;
    }

    /**
     * Native support extracted from `m_pGraphics->ySizeOf(0)` reads in CBaseFont::GetTextWidth @0045CB59 and
     * CBitmapFont::DrawTextInternal @0045E531.
     */
    public final int getFrameHeight() {
        return graphics.ySizeOf(0);
    }

    /**
     * Native support extracted from FUN_0045D2B5, used by CBaseFont::FormatText @0045D479.
     * Fully ported.
     */
    private static List<String> splitCrLfParagraphs(String text) {
        ArrayList<String> paragraphs = new ArrayList<>();
        String remaining = text;
        while (!remaining.isEmpty()) {
            int separator = remaining.indexOf('\r');
            if (separator < 0) {
                break;
            }
            if (separator + 1 >= remaining.length() || remaining.charAt(separator + 1) != '\n') {
                break;
            }
            paragraphs.add(remaining.substring(0, separator + 1).stripLeading());
            remaining = remaining.substring(separator + 2).stripLeading();
        }
        if (!remaining.isEmpty()) {
            paragraphs.add(remaining);
        }
        return paragraphs;
    }
}
