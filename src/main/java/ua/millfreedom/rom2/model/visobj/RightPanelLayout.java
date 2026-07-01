package ua.millfreedom.rom2.model.visobj;

public final class RightPanelLayout {
    // not ported. Java scalable gameplay-sidebar width.
    public static final int PANEL_WIDTH = 0xA0;
    // not ported. Java scalable gameplay-sidebar left chrome strip width.
    public static final int LEFT_STRIP_WIDTH = 0x10;
    // not ported. Java scalable gameplay-sidebar minimap panel height.
    public static final int MINIMAP_HEIGHT = 0x9E;
    // not ported. Java scalable gameplay-sidebar order toolbar height.
    public static final int ORDER_TOOLBAR_HEIGHT = 0x50;
    // not ported. Java scalable gameplay-sidebar portrait panel height.
    public static final int PORTRAIT_PANEL_HEIGHT = 0xF2;
    // not ported. Java scalable gameplay-sidebar bottom info panel height.
    public static final int INFO_PANEL_HEIGHT = 0xF2;
    // not ported. Java responsive threshold for the 800x600-style right-panel tier.
    public static final int MEDIUM_TIER_MIN_HEIGHT = 0x258;
    // not ported. Java responsive threshold for the 1024x768-style right-panel tier.
    public static final int HIGH_TIER_MIN_HEIGHT = 0x300;

    // not ported. Relative top of the minimap panel.
    public final int minimapTop;
    // not ported. Relative bottom of the minimap panel.
    public final int minimapBottom;
    // not ported. Relative top of the order toolbar panel.
    public final int orderToolbarTop;
    // not ported. Relative bottom of the order toolbar panel.
    public final int orderToolbarBottom;
    // not ported. Relative top of repeated responsive-tier filler.
    public final int extraFillTop;
    // not ported. Relative top of the portrait panel.
    public final int portraitTop;
    // not ported. Relative bottom of the portrait panel.
    public final int portraitBottom;
    // not ported. Relative top of the native side-status visual object.
    public final int sideStatusTop;
    // not ported. Relative bottom of the native side-status visual object.
    public final int sideStatusBottom;
    // not ported. Relative top where the high-tier info background is drawn.
    public final int statusInfoTop;
    // not ported. Relative bottom where the high-tier info background is drawn.
    public final int statusInfoBottom;

    /**
     * not ported. Java layout policy for scalable gameplay-sidebar composition.
     */
    public static RightPanelLayout forScreenHeight(int screenHeight) {
        return new RightPanelLayout(screenHeight);
    }

    /**
     * not ported. Java layout policy for scalable gameplay-sidebar composition.
     */
    private RightPanelLayout(int screenHeight) {
        minimapTop = 0;
        minimapBottom = MINIMAP_HEIGHT;
        orderToolbarTop = minimapBottom;
        orderToolbarBottom = orderToolbarTop + ORDER_TOOLBAR_HEIGHT;
        portraitTop = orderToolbarBottom;
        portraitBottom = portraitTop + PORTRAIT_PANEL_HEIGHT;
        sideStatusTop = portraitBottom;
        sideStatusBottom = screenHeight;
        if (screenHeight > HIGH_TIER_MIN_HEIGHT) {
            statusInfoTop = portraitBottom;
            statusInfoBottom = statusInfoTop + INFO_PANEL_HEIGHT;
            extraFillTop = statusInfoBottom;
        } else {
            statusInfoBottom = screenHeight;
            statusInfoTop = statusInfoBottom - INFO_PANEL_HEIGHT;
            extraFillTop = sideStatusTop;
        }
    }

    /**
     * not ported. Java ceil-division for repeated extra fill tiles between the fixed native top panels and bottom info.
     */
    public int extraFillRepeatCount(int extraTileHeight) {
        int spaceLeft = extraFillBottom() - extraFillTop;
        if (spaceLeft <= 0) {
            return 0;
        }
        return (spaceLeft + extraTileHeight - 1) / extraTileHeight;
    }

    /**
     * not ported. Java keeps the full side-status info panel in the 1024x768-style tier.
     */
    public boolean hasStatusInfoPanel() {
        return usesHighResolutionArt() && sideStatusBottom - sideStatusTop >= INFO_PANEL_HEIGHT;
    }

    /**
     * not ported. Java extension of the native extra-fill area for arbitrary monitor heights.
     */
    public int extraFillBottom() {
        return hasStatusInfoPanel() && !usesTallExtraFillArt() ? statusInfoTop : sideStatusBottom;
    }

    /**
     * not ported. Java responsive high-resolution art tier for monitor heights matching the old 1024x768 mode or taller.
     */
    public boolean usesHighResolutionArt() {
        return sideStatusBottom >= HIGH_TIER_MIN_HEIGHT;
    }

    /**
     * not ported. Java repeats the taller 800px extra filler only above the native 1024x768 height.
     */
    public boolean usesTallExtraFillArt() {
        return sideStatusBottom > HIGH_TIER_MIN_HEIGHT;
    }

    /**
     * not ported. Java responsive medium-resolution art tier for monitor heights matching the old 800x600 mode.
     */
    public boolean usesMediumResolutionArt() {
        return sideStatusBottom >= MEDIUM_TIER_MIN_HEIGHT && !usesHighResolutionArt();
    }
}
