package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.res.Resources;

import static ua.millfreedom.rom2.res.Constants.FONT1;
import static ua.millfreedom.rom2.res.Constants.FONT2;
import static ua.millfreedom.rom2.res.Constants.FONT3;
import static ua.millfreedom.rom2.res.Constants.FONT4;
import static ua.millfreedom.rom2.res.Constants.GRAPHICS;

/**
 * Java/Ghidra grouping owner for native shared font globals.
 */
public final class GameFonts {
    // Native global gFont1 @0061FAC0.
    public CBitmapFont font1;

    // Native global gFont2 @00620500.
    public CBitmapFont font2;

    // Native global gFont3 @00620E30.
    public CBitmapFont font3;

    // Native global gFont4 @00620DC8.
    public CA16Font font4;

    /**
     * Java support constructor for the shared font wrapper.
     * not ported.
     */
    public GameFonts() {
    }

    /**
     * Native: GameFonts::loadFonts @0045F589.
     * Fully ported. Native allocates the four shared font globals from graphics/font1..font4 with spacing `2`.
     */
    public void loadFonts() {
        font1 = new CBitmapFont(Resources.path(GRAPHICS, FONT1, FONT1), 2);
        font2 = new CBitmapFont(Resources.path(GRAPHICS, FONT2, FONT2), 2);
        font3 = new CBitmapFont(Resources.path(GRAPHICS, FONT3, FONT3), 2);
        font4 = new CA16Font(Resources.path(GRAPHICS, FONT4, FONT4), 2);
    }
}
