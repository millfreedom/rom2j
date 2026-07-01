package ua.millfreedom.rom2.model.render;

/**
 * Holds the centered aspect-fit presentation rectangle for a logical surface.
 * not ported.
 */
public record AspectFitLayout(
        double drawX,
        double drawY,
        double drawWidth,
        double drawHeight,
        double scale) {

    /**
     * Calculates the largest centered rectangle that preserves the logical surface aspect ratio.
     * not ported.
     */
    public static AspectFitLayout fit(int availableWidth, int availableHeight, int contentWidth, int contentHeight) {
        double scale = Math.min(
                availableWidth / (double) contentWidth,
                availableHeight / (double) contentHeight
        );
        double drawWidth = contentWidth * scale;
        double drawHeight = contentHeight * scale;
        double drawX = (availableWidth - drawWidth) * 0.5;
        double drawY = (availableHeight - drawHeight) * 0.5;
        return new AspectFitLayout(drawX, drawY, drawWidth, drawHeight, scale);
    }
}
