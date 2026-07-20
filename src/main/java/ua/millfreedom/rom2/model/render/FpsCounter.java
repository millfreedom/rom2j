package ua.millfreedom.rom2.model.render;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CBitmapFont;
import ua.millfreedom.rom2.model.color.RGB32;
import ua.millfreedom.rom2.model.palette.Palettes;

/**
 * Static Java-only FPS overlay for the LWJGL presentation shell.
 */
public final class FpsCounter {
    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final int BOX_LEFT = 0;
    private static final int BOX_TOP = 0;
    private static final int BOX_PADDING_X = 4;
    private static final int BOX_PADDING_Y = 3;
    private static final int BOX_COLOR = RGB32.from(0x08, 0x08, 0x08);
    private static final int BOX_BORDER_COLOR = RGB32.from(0x60, 0x60, 0x60);

    private static long lastSampleNanos;
    private static int framesThisSample;
    private static String label = "FPS";
    private static final CBitmapFont font = Globals.fonts.font2;

    /**
     * Static utility constructor.
     * not ported.
     */
    private FpsCounter() {
    }

    /**
     * Updates FPS accounting and draws the current FPS overlay.
     * not ported.
     */
    public static void draw() {
        updateCounter();

        int textWidth = font.getTextWidth(label);
        int textHeight = font.getFrameHeight();
        int right = BOX_LEFT + textWidth + BOX_PADDING_X * 2;
        int bottom = BOX_TOP + textHeight + BOX_PADDING_Y * 2;

        Globals.renderer.applyShadeToRect(BOX_LEFT, BOX_TOP, right, bottom, 6);
        //Globals.renderer.drawRect(BOX_LEFT, BOX_TOP, right, bottom, BOX_BORDER_COLOR);
        font.drawTextInternal(
                BOX_LEFT + BOX_PADDING_X,
                BOX_TOP + BOX_PADDING_Y,
                label,
                0,
                Palettes.greenish
        );
    }

    /**
     * Updates the cached FPS label once per elapsed second.
     * not ported.
     */
    private static void updateCounter() {
        long now = System.nanoTime();
        if (lastSampleNanos == 0L) {
            lastSampleNanos = now;
        }

        framesThisSample++;
        long elapsed = now - lastSampleNanos;
        if (elapsed >= NANOS_PER_SECOND) {
            int fps = (int) ((framesThisSample * NANOS_PER_SECOND) / elapsed);
            label = "FPS " + fps;
            framesThisSample = 0;
            lastSampleNanos = now;
        }
    }
}
