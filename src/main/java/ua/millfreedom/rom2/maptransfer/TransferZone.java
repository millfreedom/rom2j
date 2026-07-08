package ua.millfreedom.rom2.maptransfer;

/**
 * Java-only installed source coordinate for dedicated map transfer.
 * not ported.
 */
public final class TransferZone {
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
    private final int sourceLineNumber;

    /**
     * Java support constructor for an active-map transfer zone.
     * not ported.
     */
    public TransferZone(
            int sourceX,
            int sourceY,
            String destinationMapName,
            int destinationX,
            int destinationY,
            int sourceLineNumber
    ) {
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.destinationMapName = destinationMapName;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
        this.sourceLineNumber = sourceLineNumber;
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
     * Java support accessor for the destination map name.
     * not ported.
     */
    public String destinationMapName() {
        return destinationMapName;
    }

    /**
     * Java support accessor for the destination x cell.
     * not ported.
     */
    public int destinationX() {
        return destinationX;
    }

    /**
     * Java support accessor for the destination y cell.
     * not ported.
     */
    public int destinationY() {
        return destinationY;
    }

    /**
     * Java support accessor for the source `portals.txt` line number.
     * not ported.
     */
    public int sourceLineNumber() {
        return sourceLineNumber;
    }
}
