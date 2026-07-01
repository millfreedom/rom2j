package ua.millfreedom.rom2.model.world;

/**
 * Native NeighborStepTable structure embedded in CWorldMap.
 * Size in native code: 0x30.
 */
public final class NeighborStepTable {
    //0x00
    public final byte[] dx = new byte[8];

    //0x08
    public final byte[] dy = new byte[8];

    //0x10
    public final int[] cellDelta = new int[8];
}
