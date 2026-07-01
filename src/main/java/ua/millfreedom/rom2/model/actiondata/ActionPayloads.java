package ua.millfreedom.rom2.model.actiondata;

import ua.millfreedom.rom2.model.action.Property;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * not ported.
 */
public final class ActionPayloads {
    /**
     * not ported.
     */
    private ActionPayloads() {
    }

    /**
     * Native support extracted from ShortArrayBlobAction::Action2 @0050C7AE.
     */
    public static short[] getShortArray(Property<byte[]> values) {
        short[] result = new short[values.size() / Short.BYTES];
        for (int index = 0; index < result.length; index++) {
            result[index] = (short) values.unsignedWordAtIndex(index);
        }
        return result;
    }

    /**
     * Native support extracted from CMainWindow::onDialogClosed @004891D8 and MapVisualObject::sendDiplomacyRelationsAction @0041A17A.
     */
    public static void setShortArray(Property<Integer> count, Property<byte[]> values, short[] source) {
        Objects.requireNonNull(source, "source");
        count.set(source.length);
        setShortArray(values, source);
    }

    /**
     * Native support extracted from BitmaskShortListAction::Action1 @0050CCEB.
     */
    public static void setShortArray(Property<byte[]> values, short[] source) {
        Objects.requireNonNull(source, "source");
        values.fill((byte) 0);
        int elementCount = Math.min(source.length, values.size() / Short.BYTES);
        for (int index = 0; index < elementCount; index++) {
            values.setUnsignedWordAtIndex(index, source[index]);
        }
    }

    /**
     * Native support extracted from ItemListAction::Action1 @0050C8AB and Item::appendNetworkItemPayload @005241BF.
     */
    public static int appendTrailingByte(Property<Integer> length, Property<byte[]> data, int value) {
        byte[] bytes = growTrailingData(length, data, Byte.BYTES);
        int relativeOffset = bytes.length - Byte.BYTES;
        bytes[relativeOffset] = (byte) value;
        data.set(bytes);
        return relativeOffset;
    }

    /**
     * Native support extracted from ItemListAction::Action1 @0050C8AB and Item::appendNetworkItemPayload @005241BF.
     */
    public static void appendTrailingShort(Property<Integer> length, Property<byte[]> data, int value) {
        byte[] bytes = growTrailingData(length, data, Short.BYTES);
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putShort(bytes.length - Short.BYTES, (short) value);
        data.set(bytes);
    }

    /**
     * Native support extracted from Item::appendItemInfoPacket @00524147.
     */
    public static void rewriteTrailingShort(Property<Integer> length, Property<byte[]> data, int relativeOffset, int value) {
        byte[] bytes = data.get();
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putShort(relativeOffset, (short) value);
        length.set(bytes.length);
        data.set(bytes);
    }

    /**
     * Native support extracted from ItemListAction::Action1 @0050C8AB and Item::appendNetworkItemPayload @005241BF.
     */
    public static void appendTrailingInt(Property<Integer> length, Property<byte[]> data, int value) {
        byte[] bytes = growTrailingData(length, data, Integer.BYTES);
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(bytes.length - Integer.BYTES, value);
        data.set(bytes);
    }

    /**
     * Native support extracted from Item::appendNetworkItemPayload @005241BF.
     */
    public static void rewriteTrailingByte(Property<Integer> length, Property<byte[]> data, int relativeOffset, int value) {
        byte[] bytes = data.get();
        if (relativeOffset < 0 || relativeOffset >= bytes.length) {
            throw new IndexOutOfBoundsException("Item-list trailing payload offset " + relativeOffset);
        }
        bytes[relativeOffset] = (byte) value;
        length.set(bytes.length);
        data.set(bytes);
    }

    /**
     * not ported.
     */
    private static byte[] growTrailingData(Property<Integer> length, Property<byte[]> data, int addedSize) {
        byte[] current = data.get();
        byte[] grown = new byte[current.length + addedSize];
        System.arraycopy(current, 0, grown, 0, current.length);
        length.set(grown.length);
        return grown;
    }
}
