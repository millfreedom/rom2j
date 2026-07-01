package ua.millfreedom.rom2.model.world;

/**
 * Native Rect16 structure embedded in CWorldMap.
 * Size in native code: 0x4.
 */
public final class Rect16 {
    //0x0
    public final Point8 lt = new Point8();

    //0x2
    public final Point8 rb = new Point8();
}
