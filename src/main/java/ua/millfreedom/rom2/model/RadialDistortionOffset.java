package ua.millfreedom.rom2.model;

/**
 * Native support structure used by RadialScreenDistortion @004A6840.
 */
public final class RadialDistortionOffset {
    //0x00
    public final short sourceXDelta;
    //0x02
    public final short sourceYDelta;

    /**
     * Native support constructor for RadialScreenDistortion::buildOffsetTable @004A695F entries.
     */
    public RadialDistortionOffset(int sourceXDelta, int sourceYDelta) {
        this.sourceXDelta = (short) sourceXDelta;
        this.sourceYDelta = (short) sourceYDelta;
    }
}
