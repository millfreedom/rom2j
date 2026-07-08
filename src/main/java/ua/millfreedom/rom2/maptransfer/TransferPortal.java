package ua.millfreedom.rom2.maptransfer;

import static ua.millfreedom.rom2.GameServer.MAP_EXTENSION;

/**
 * Java-only parsed `portals.txt` entry for dedicated map transfer.
 * not ported.
 */
public final class TransferPortal {
    // Java support, not a native field.
    private final String sourceMapName;
    // Java support, not a native field.
    private final int sourceX;
    // Java support, not a native field.
    private final int sourceY;
    // Java support, not a native field.
    private final String destinationMapName;
    // Java support, not a native field.
    private final int destinationX;
    // Java support, not a native field.
    private final int destinationY;
    // Java support, not a native field.
    private final int lineNumber;

    /**
     * Java support constructor for one parsed transfer portal.
     * not ported.
     */
    public TransferPortal(
            String sourceMapName,
            int sourceX,
            int sourceY,
            String destinationMapName,
            int destinationX,
            int destinationY,
            int lineNumber
    ) {
        this.sourceMapName = sourceMapName;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.destinationMapName = destinationMapName;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
        this.lineNumber = lineNumber;
    }

    /**
     * Java support case-insensitive map-name matcher for current dedicated map selection.
     * not ported.
     */
    public boolean sourceMapMatches(String mapName) {
        return sourceMapName.equalsIgnoreCase(mapName) || (sourceMapName + MAP_EXTENSION).equalsIgnoreCase(mapName);
    }

    /**
     * Java support key for duplicate source-coordinate detection on one map.
     * not ported.
     */
    public long sourceCoordinateKey() {
        return ((long) sourceX << Integer.SIZE) ^ (sourceY & 0xFFFF_FFFFL);
    }

    /**
     * Java support conversion from a parsed file entry to an installed transfer zone.
     * not ported.
     */
    public TransferZone toTransferZone() {
        return new TransferZone(sourceX, sourceY, destinationMapName, destinationX, destinationY, lineNumber);
    }

    /**
     * Java support accessor for the source x cell.
     * not ported.
     */
    public int sourceX() {
        return sourceX;
    }

    /**
     * Java support accessor for the source y cell.
     * not ported.
     */
    public int sourceY() {
        return sourceY;
    }

    /**
     * Java support accessor for the original file line number.
     * not ported.
     */
    public int lineNumber() {
        return lineNumber;
    }
}
