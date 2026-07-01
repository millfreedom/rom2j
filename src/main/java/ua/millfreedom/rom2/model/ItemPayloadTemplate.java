package ua.millfreedom.rom2.model;

import java.util.Arrays;

public final class ItemPayloadTemplate {
    // Native itemname.pkt entry byte at offset +0x04.
    public final int staticFlags;

    // Native itemname.pkt entry byte at offset +0x05.
    public final int payloadEntryCount;

    // Native itemname.pkt payload bytes beginning at entry offset +0x07.
    public final byte[] payloadBytes;

    /**
     * Native support container for DAT_006225C0 entries initialized by ItemNames::loadItemNames @00474268.
     */
    public ItemPayloadTemplate(int staticFlags, int payloadEntryCount, byte[] payloadBytes) {
        this.staticFlags = staticFlags & 0xFF;
        this.payloadEntryCount = payloadEntryCount & 0xFF;
        this.payloadBytes = Arrays.copyOf(payloadBytes, payloadBytes.length);
    }
}
