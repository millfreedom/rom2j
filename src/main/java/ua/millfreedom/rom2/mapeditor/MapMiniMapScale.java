package ua.millfreedom.rom2.mapeditor;

/**
 * Editor-owned minimap scale choices matching the native editor Help labels.
 * not ported.
 */
enum MapMiniMapScale {
    HALF("1:2", 1, 2),
    NORMAL("1:1", 1, 1),
    DOUBLE("2:1", 2, 1);

    private final String label;
    private final int numerator;
    private final int denominator;

    /**
     * Java support constructor for editor minimap scale metadata.
     * not ported.
     */
    MapMiniMapScale(String label, int numerator, int denominator) {
        this.label = label;
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /**
     * Java support display label for minimap scale commands.
     * not ported.
     */
    String label() {
        return label;
    }

    /**
     * Java support map-cell span scaling for the editor minimap overview.
     * not ported.
     */
    int scaledSpan(int cellSpan) {
        return Math.max(1, (cellSpan * numerator + denominator - 1) / denominator);
    }
}
