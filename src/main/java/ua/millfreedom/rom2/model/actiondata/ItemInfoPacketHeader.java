package ua.millfreedom.rom2.model.actiondata;

import ua.millfreedom.rom2.model.action.ItemListAction;

public class ItemInfoPacketHeader {
    //0x00
    public int packedItemHash;

    //0x02
    public int count;

    //0x04
    public int flags;

    //0x05
    public int descriptorCount;

    //0x06
    public int descriptorByteLength;

    /**
     * Native support structure used by Item::writeInfoPacket @005244D6.
     */
    public ItemInfoPacketHeader() {
    }

    /**
     * Native support extracted from Item::appendItemInfoPacket @00524147.
     * Fully ported.
     */
    public void writeTo(ItemListAction action, int headerOffset) {
        ActionPayloads.rewriteTrailingShort(action.trailingDataLength, action.trailingData, headerOffset, packedItemHash);
        ActionPayloads.rewriteTrailingShort(action.trailingDataLength, action.trailingData, headerOffset + 2, count);
        ActionPayloads.rewriteTrailingByte(action.trailingDataLength, action.trailingData, headerOffset + 4, flags);
        ActionPayloads.rewriteTrailingByte(action.trailingDataLength, action.trailingData, headerOffset + 5, descriptorCount);
        ActionPayloads.rewriteTrailingByte(action.trailingDataLength, action.trailingData, headerOffset + 6, descriptorByteLength);
    }

}
