package ua.millfreedom.rom2.model.render;

import ua.millfreedom.rom2.model.Screen;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * Swing presentation bridge for the software renderer.
 * not ported.
 */
public class SwingRenderer extends SoftRenderer {
    private final BufferedImage presentationImage;
    private final int[] presentationPixels;

    /**
     * Java support constructor for Swing-backed software rendering.
     * not ported.
     */
    public SwingRenderer(Screen screen) {
        super(screen);
        this.presentationImage = new BufferedImage(screen.w(), screen.h(), BufferedImage.TYPE_INT_ARGB);
        this.presentationPixels = ((DataBufferInt) presentationImage.getRaster().getDataBuffer()).getData();
    }

    /**
     * Java support presentation boundary for copying the software straight-ARGB surface into Swing image storage.
     * not ported.
     */
    @Override
    public void presentSurface(int framebufferWidth, int framebufferHeight) {
        int[] sourcePixels = screen.surface();
        int sourcePitchPixels = screen.pitchPixels();
        int width = screen.w();
        int height = screen.h();
        for (int y = 0; y < height; y++) {
            int sourceOffset = (screen.y() + y) * sourcePitchPixels + screen.x();
            int targetOffset = y * width;
            System.arraycopy(sourcePixels, sourceOffset, presentationPixels, targetOffset, width);
        }
    }

    /**
     * Java support drawing of the last presented software surface into a Swing graphics target.
     * not ported.
     */
    public void drawTo(Graphics2D graphics, int left, int top, int width, int height) {
        graphics.drawImage(presentationImage, left, top, width, height, null);
    }

    /**
     * Java support direct ARGB rectangle fill for editor-only Swing preview scaffolding.
     * not ported.
     */
    public void fillArgbRect(int left, int top, int right, int bottom, int argb) {
        int clippedLeft = Math.max(left, clipLeft);
        int clippedTop = Math.max(top, clipTop);
        int clippedRight = Math.min(right, clipRight);
        int clippedBottom = Math.min(bottom, clipBottom);
        if (clippedLeft >= clippedRight || clippedTop >= clippedBottom) {
            return;
        }

        int pitchPixels = activeRenderTarget.pitchPixels();
        int surfaceLeft = activeRenderTarget.x();
        int surfaceTop = activeRenderTarget.y();
        int[] surfacePixels = activeRenderTarget.surface();
        for (int y = clippedTop; y < clippedBottom; y++) {
            int rowOffset = (y - surfaceTop) * pitchPixels + (clippedLeft - surfaceLeft);
            int rowEnd = rowOffset + clippedRight - clippedLeft;
            Arrays.fill(surfacePixels, rowOffset, rowEnd, argb);
        }
    }
}
