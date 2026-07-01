package ua.millfreedom.rom2;

/**
 * Native packet payload buffer consumed by CBufferManager.
 */
public class NetBuffer {
    public static final int WIRE_HEADER_SIZE = 8;
    public static final int PAYLOAD_CAPACITY = 0x8E;

    //0x00
    public int queuedAtTick;
    //0x04
    public int lastSendTick;
    //0x08
    public NetBufferInfo header;
    //0x10
    public byte[] payload;
    //0xA0
    public int payloadSize;
    //0xA4
    public int readPosition;

    /**
     * Native: NetBuffer::New @004FFA94.
     * Fully ported.
     */
    public NetBuffer() {
        header = new NetBufferInfo();
        payload = new byte[PAYLOAD_CAPACITY];
        payloadSize = -1;
        readPosition = -1;
        queuedAtTick = 0;
        lastSendTick = 0;
    }

    /**
     * Native: NetBuffer::Clear @004FFB8C.
     * Fully ported.
     */
    public NetBuffer Clear() {
        payloadSize = 0;
        readPosition = 0;
        header.segmentMarkerCount = 0;
        queuedAtTick = 0;
        lastSendTick = 0;
        header.compressedBitCount = 0;
        return this;
    }

    /**
     * Native: NetBuffer::GetFreeSize @0053F140.
     * Fully ported.
     */
    public int GetFreeSize() {
        return PAYLOAD_CAPACITY - payloadSize;
    }

    /**
     * Native: NetBuffer::AppendBytes @004FFBD4.
     * Fully ported.
     */
    public boolean AppendBytes(byte[] source, int sourceOffset, int size) {
        int newSize = payloadSize + size;
        if (newSize < PAYLOAD_CAPACITY + 1) {
            System.arraycopy(source, sourceOffset, payload, payloadSize, size);
            payloadSize = newSize;
        }
        return newSize < PAYLOAD_CAPACITY + 1;
    }

    /**
     * Native: NetBuffer::GetRemainingPayloadSize @0053F210.
     * Fully ported.
     */
    public int GetRemainingPayloadSize() {
        return payloadSize - readPosition;
    }

    /**
     * Native: NetBuffer::GetWireSize @0053EC90.
     * Fully ported.
     */
    public int GetWireSize() {
        return payloadSize + WIRE_HEADER_SIZE;
    }

    /**
     * Native: NetBuffer::SetPayloadSize @0053F250.
     * Fully ported.
     */
    public void SetPayloadSize(int payloadSize) {
        this.payloadSize = payloadSize;
    }

    /**
     * Native: NetBuffer::GetHeader @0053EC70 and @0053F230.
     * Fully ported.
     */
    public NetBufferInfo GetHeader() {
        return header;
    }

    /**
     * Native: NetBuffer::HasRemainingPayload @0053F2D0.
     * Fully ported.
     */
    public boolean HasRemainingPayload() {
        return readPosition < payloadSize;
    }

    /**
     * Native: NetBuffer::GetQueuedAge @00540E80.
     * Fully ported.
     */
    public int GetQueuedAge(int nowTick) {
        return Integer.compareUnsigned(queuedAtTick, nowTick) < 0 ? nowTick - queuedAtTick : 0;
    }

    /**
     * Native: NetBuffer::SetQueuedAtTick @00540EB0.
     * Fully ported.
     */
    public void SetQueuedAtTick(int queuedAtTick) {
        this.queuedAtTick = queuedAtTick;
    }

    /**
     * Native: NetBuffer::SetLastSendTick @00540ED0.
     * Fully ported.
     */
    public void SetLastSendTick(int lastSendTick) {
        this.lastSendTick = lastSendTick;
    }

    /**
     * Native: NetBuffer::GetLastSendAge @00540EF0.
     * Fully ported.
     */
    public int GetLastSendAge(int nowTick) {
        return Integer.compareUnsigned(lastSendTick, nowTick) < 0 ? nowTick - lastSendTick : 0;
    }

    /**
     * Native: NetBuffer::IncrementSegmentMarkerCount @00540430.
     * Fully ported.
     */
    public void IncrementSegmentMarkerCount() {
        header.segmentMarkerCount++;
    }

    /**
     * Native: NetBuffer::GetSegmentMarkerCount @0053F300.
     * Fully ported.
     */
    public int GetSegmentMarkerCount() {
        return header.segmentMarkerCount;
    }

    /**
     * Native: NetBuffer::ConsumeSegmentMarker @0053F320.
     * Fully ported.
     */
    public void ConsumeSegmentMarker() {
        header.segmentMarkerCount--;
    }

    /**
     * Native: NetBuffer::CopyFrom @004FFADA.
     * Fully ported.
     */
    public boolean CopyFrom(NetBuffer from) {
        header.payloadLengthOrSequence = from.header.payloadLengthOrSequence;
        header.compressionStreamId = from.header.compressionStreamId;
        header.compressedBitCount = from.header.compressedBitCount;
        header.segmentMarkerCount = from.header.segmentMarkerCount;
        System.arraycopy(from.payload, 0, payload, 0, from.payloadSize);
        payloadSize = from.payloadSize;
        readPosition = from.readPosition;
        queuedAtTick = from.queuedAtTick;
        lastSendTick = from.lastSendTick;
        return true;
    }

    /**
     * Native: NetBuffer::ReadBytes @004FFC34.
     * Fully ported.
     */
    public void ReadBytes(byte[] target, int targetOffset, int size) {
        if (readPosition + size <= payloadSize) {
            System.arraycopy(payload, readPosition, target, targetOffset, size);
            readPosition += size;
        }
    }

    /**
     * Native: NetBuffer::ReadByte @004FFC97.
     * Fully ported.
     */
    public int ReadByte() {
        readPosition++;
        return payload[readPosition - 1] & 0xFF;
    }

}
