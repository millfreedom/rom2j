package ua.millfreedom.rom2.mapeditor;

/**
 * Help-aligned landscape choices for the standalone MapEditor New Map dialog.
 * not ported.
 */
enum MapEditorLandscapeType {
    GRASSLAND("Grassland", 0),
    BARE_LAND("Bare land", 1),
    SAVANNA("Savanna", 2),
    SAND_DESERT("Sand (Desert)", 3),
    DRY_LAND_CRACKED("Dry land (cracked)", 4),
    STONY_LAND("Stony land", 5),
    GRASS_AND_FLOWERS("Covered with grass and flowers", 6),
    HIGH_MOUNTAIN_CLIFFS("High Mountain Cliffs", 7),
    WATER("Water", 8);

    private static final int TERRAIN_TILE_INDEX_MASK = 0x1FFF;
    private static final int TERRAIN_FAMILY_SHIFT = 6;
    private static final int DEFAULT_TILE_RANDOM_BITS = 0x11;

    private final String label;
    private final int terrainFamily;

    /**
     * Java support constructor for Help landscape names and existing terrain-family ids.
     * not ported.
     */
    MapEditorLandscapeType(String label, int terrainFamily) {
        this.label = label;
        this.terrainFamily = terrainFamily;
    }

    /**
     * Java support terrain tile word for a blank map using this landscape family.
     * not ported.
     */
    int defaultTileWord() {
        return (terrainFamily << TERRAIN_FAMILY_SHIFT) | DEFAULT_TILE_RANDOM_BITS;
    }

    /**
     * Java support terrain-family extraction from the existing serialized tile-word layout.
     * not ported.
     */
    static MapEditorLandscapeType fromTileWord(int tileWord) {
        int terrainFamily = (tileWord & TERRAIN_TILE_INDEX_MASK) >> TERRAIN_FAMILY_SHIFT;
        for (MapEditorLandscapeType type : values()) {
            if (type.terrainFamily == terrainFamily) {
                return type;
            }
        }
        return GRASSLAND;
    }

    /**
     * Java support display label for Swing combo boxes.
     * not ported.
     */
    @Override
    public String toString() {
        return label;
    }
}
