package ua.millfreedom.rom2.model.world;

/**
 * Native support state embedded in CWorldMap at 0x54190.
 * Owns the path-search arrays and Path Finding registry values through CWorldMap +0x5859B.
 */
public final class WorldMapSearchScratchState {
    //0x0000
    public int field0x0000;

    //0x0004
    public int field0x0004;

    //0x0008
    public final byte[] unknown0x0008_0x03EF = new byte[0x3E8];

    //0x03F0
    public byte mode0x03F0;
    //0x03F4
    public final short[] pathFrontierCells0x03F4 = new short[0x1000];
    //0x23F4
    public final short[] nextPathFrontierCells0x23F4 = new short[0x1000];
    //0x43F4
    public int staticScanAhead0x43F4;
    //0x43F8
    public int dynamicScanAhead0x43F8;
    //0x43FC
    public int staticRefreshRate0x43FC;
    //0x4400
    public int dynamicRefreshRate0x4400;
    //0x4404
    public int dynamicByStaticLookup0x4404;
    //0x4408
    public int staticIsntNeeded0x4408;

    /**
     * Native: WorldMapSearchScratchState::Init @0055F2F0.
     */
    public void initialize() {
        field0x0000 = 0;
        field0x0004 = 0;
        mode0x03F0 = 2;
    }
}
