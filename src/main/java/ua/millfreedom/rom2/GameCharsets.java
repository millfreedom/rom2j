package ua.millfreedom.rom2;

import java.nio.charset.Charset;

/**
 * Byte encodings used only at legacy game-data and font-glyph boundaries.
 */
public final class GameCharsets {
    // not ported. CP1251 encoding for resource, save, and game-network text bytes.
    public static final Charset GAME_TEXT = Charset.forName("windows-1251");
    // not ported. CP866 byte-to-glyph mapping used only by the native bitmap font assets.
    public static final Charset FONT_GLYPHS = Charset.forName("Cp866");

    // not ported.
    private GameCharsets() {
    }
}
