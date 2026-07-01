package ua.millfreedom.rom2.model.render;

import ua.millfreedom.rom2.model.CRect;

/**
 * Holds the active source crop and target rectangle used by Java's platform presentation shell.
 * not ported.
 */
public record PresentationTransform(
        PresentationMode mode,
        int sourceLeft,
        int sourceTop,
        int sourceRight,
        int sourceBottom,
        double drawX,
        double drawY,
        double drawWidth,
        double drawHeight
) {
    /**
     * Builds a presentation transform using the native game-screen size as the outer aspect-preserving target.
     * not ported.
     */
    public static PresentationTransform create(
            PresentationMode mode,
            CRect sourceRect,
            int availableWidth,
            int availableHeight
    ) {
        AspectFitLayout layout = AspectFitLayout.fit(
                availableWidth,
                availableHeight,
                sourceRect.width(),
                sourceRect.height()
        );
        return new PresentationTransform(
                mode,
                sourceRect.left,
                sourceRect.top,
                sourceRect.right,
                sourceRect.bottom,
                layout.drawX(),
                layout.drawY(),
                layout.drawWidth(),
                layout.drawHeight()
        );
    }

    /**
     * Returns the cropped source width in native game coordinates.
     * not ported.
     */
    public int sourceWidth() {
        return sourceRight - sourceLeft;
    }

    /**
     * Returns the cropped source height in native game coordinates.
     * not ported.
     */
    public int sourceHeight() {
        return sourceBottom - sourceTop;
    }

    /**
     * Returns the X scale from source coordinates to target coordinates.
     * not ported.
     */
    public double scaleX() {
        return drawWidth / sourceWidth();
    }

    /**
     * Returns the Y scale from source coordinates to target coordinates.
     * not ported.
     */
    public double scaleY() {
        return drawHeight / sourceHeight();
    }

    /**
     * Maps a platform-window X coordinate back into native game coordinates.
     * not ported.
     */
    public int mapX(double x) {
        return mapCoordinate(x, drawX, drawWidth, sourceLeft, sourceWidth());
    }

    /**
     * Maps a platform-window Y coordinate back into native game coordinates.
     * not ported.
     */
    public int mapY(double y) {
        return mapCoordinate(y, drawY, drawHeight, sourceTop, sourceHeight());
    }

    /**
     * Maps one platform-window coordinate onto one cropped native axis while clamping to the valid source range.
     * not ported.
     */
    private static int mapCoordinate(double position, double drawStart, double drawExtent, int sourceStart, int sourceExtent) {
        if (sourceExtent <= 1 || drawExtent <= 0.0) {
            return sourceStart;
        }

        double normalized = (position - drawStart) / drawExtent;
        double clamped = Math.max(0.0, Math.min(normalized, Math.nextDown(1.0)));
        return sourceStart + (int) (clamped * sourceExtent);
    }
}
