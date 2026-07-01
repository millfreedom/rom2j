package ua.millfreedom.rom2.model.visobj;

/**
 * Native support type recovered from MultiplayerMapSelectionDialogVisualObject helper/destructor paths.
 * Purpose: available-map metadata row with internal map id, announcement text, and size/level fields.
 */
class MultiplayerMapSelectionEntry {
    //0x00
    public String internalMapId;
    //0x04
    public String displayName;
    //0x08
    public String tooltipText;
    //0x0c
    public int recommendedPlayers;
    //0x10
    public int mapLevel;
    //0x14
    public int rawMapWidth;
    //0x18
    public int rawMapHeight;

    /**
     * Native: MultiplayerMapSelectionEntry::MultiplayerMapSelectionEntry @0044E720.
     * Fully ported. Native default-constructs CString fields at 0x00, 0x04, and 0x08.
     */
    public MultiplayerMapSelectionEntry() {
        internalMapId = "";
        displayName = "";
        tooltipText = "";
    }

    /**
     * Native formatting branch inside MultiplayerMapSelectionDialogVisualObject::appendAvailableMap @0044BAB0.
     * Ported. Native formats the available-map display row as `%s#%dx%d#%d#%d`.
     */
    public String formatDisplayRow() {
        String mapName = displayName == null ? "" : displayName;
        return mapName
                + "#"
                + getPlayableWidth()
                + "x"
                + getPlayableHeight()
                + "#"
                + recommendedPlayers
                + "#"
                + mapLevel;
    }

    /**
     * Native size-adjustment branch inside MultiplayerMapSelectionDialogVisualObject::appendAvailableMap @0044BAB0.
     */
    public int getPlayableWidth() {
        return rawMapWidth - 0x10;
    }

    /**
     * Native size-adjustment branch inside MultiplayerMapSelectionDialogVisualObject::appendAvailableMap @0044BAB0.
     */
    public int getPlayableHeight() {
        return rawMapHeight - 0x10;
    }
}
