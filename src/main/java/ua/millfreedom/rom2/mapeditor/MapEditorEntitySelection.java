package ua.millfreedom.rom2.mapeditor;

/**
 * Editor-owned selection target for saved entities rendered on the map surface.
 * not ported.
 */
final class MapEditorEntitySelection {
    enum Kind {
        OBJECT,
        BUILDING,
        SHOP_DESCRIPTOR,
        INN_DESCRIPTOR,
        POST_DESCRIPTOR,
        DROP_LOCATION_INSTANT,
        UNIT,
        SACK,
        EFFECT,
        MUSIC
    }

    final Kind kind;
    final int index;

    /**
     * Java support constructor for viewport entity selections.
     * not ported.
     */
    MapEditorEntitySelection(Kind kind, int index) {
        this.kind = kind;
        this.index = index;
    }
}
