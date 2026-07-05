package ua.millfreedom.rom2.mapeditor;

/**
 * Help-aligned editor tool modes for the standalone MapEditor surface.
 * not ported.
 */
public enum MapEditorToolMode {
    SELECT("Select", "Choose a map area for group operations"),
    ENTITY_SELECT("Entity Select", "Select saved map entities and open their property page"),
    TERRAIN("Terrain", "Edit surface type"),
    ALTITUDE("Altitude", "Change tile altitude"),
    OBJECTS("Objects", "Place or erase map object bytes"),
    BUILDINGS("Buildings", "Place and configure buildings"),
    BRIDGES("Bridges", "Place dynamic-size bridges"),
    ITEMS("Items", "Place bags and unit inventory items"),
    UNITS("Units", "Place and configure units"),
    MUSIC("Music", "Configure interactive music areas"),
    LOGIC("Logic", "Configure instants, checks, triggers, traps, and casting"),
    GRID("Grid", "Toggle tile-grid overlay"),
    RANDOMIZATION("Randomization", "Randomize terrain and altitude"),
    LIGHT("Light", "Configure global map lighting");

    private final String label;
    private final String description;

    /**
     * Java support constructor for editor tool-mode metadata.
     * not ported.
     */
    MapEditorToolMode(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /**
     * Java support display label for editor tool selectors.
     * not ported.
     */
    public String label() {
        return label;
    }

    /**
     * Java support description copied from native editor Help feature surfaces.
     * not ported.
     */
    public String description() {
        return description;
    }

    /**
     * Java support predicate for modes implemented by the first isolated editor slice.
     * not ported.
     */
    public boolean editsCellsDirectly() {
        return this == TERRAIN || this == ALTITUDE || this == OBJECTS;
    }

    /**
     * Java support predicate for direct-edit modes that paint continuously while dragging.
     * not ported.
     */
    public boolean paintsOnDrag() {
        return this == TERRAIN || this == ALTITUDE;
    }

    /**
     * Java support display text for Swing combo boxes.
     * not ported.
     */
    @Override
    public String toString() {
        return label;
    }
}
