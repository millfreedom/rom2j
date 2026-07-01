package ua.millfreedom.rom2;

/**
 * Native NetBuffer wire header.
 */
public class NetBufferInfo {
    //0x00
    public int payloadLengthOrSequence;
    //0x04
    public int compressionStreamId;
    //0x05
    public int compressedBitCount;
    //0x07
    public int segmentMarkerCount;

    /**
     * Native: NetBufferInfo::NetBufferInfo @0053EC40.
     * Fully ported.
     */
    public NetBufferInfo() {
        Clear();
    }

    /**
     * Native: NetBufferInfo::Clear @0053ECB0.
     * Fully ported.
     */
    public void Clear() {
        payloadLengthOrSequence = 0;
        compressionStreamId = 0;
        compressedBitCount = 0;
        segmentMarkerCount = 0;
    }
}
