package ua.millfreedom.rom2.model.action;

import java.nio.ByteBuffer;
import java.util.function.IntSupplier;

/**
 * Describes one typed field stored at a native-compatible action payload offset.
 */
public final class Property<T> {
    private final ByteBuffer buffer;
    private final int startPosition;
    private final IntSupplier sizeSupplier;
    private final Class<T> type;
    private final Marshaller<T> marshaller;

    /**
     * not ported.
     */
    public Property(ByteBuffer buffer, int startPosition, int size, Class<T> type) {
        this(buffer, startPosition, fixedSize(size), type, defaultMarshaller(size, type));
    }

    /**
     * not ported.
     */
    public Property(ByteBuffer buffer, int startPosition, int size, Class<T> type, Marshaller<T> marshaller) {
        this(buffer, startPosition, fixedSize(size), type, marshaller);
    }

    /**
     * not ported.
     */
    public Property(ByteBuffer buffer, int startPosition, IntSupplier sizeSupplier, Class<T> type, Marshaller<T> marshaller) {
        if (buffer == null || startPosition < 0 || sizeSupplier == null || type == null || marshaller == null) {
            throw new IllegalArgumentException("Invalid property descriptor");
        }
        this.buffer = buffer;
        this.startPosition = startPosition;
        this.sizeSupplier = sizeSupplier;
        this.type = type;
        this.marshaller = marshaller;
    }

    /**
     * not ported.
     */
    public static Property<Integer> unsignedByte(ByteBuffer buffer, int startPosition) {
        return new Property<>(buffer, startPosition, Byte.BYTES, Integer.class, typed(StandardMarshaller.UNSIGNED_BYTE));
    }

    /**
     * not ported.
     */
    public static Property<Integer> unsignedWord(ByteBuffer buffer, int startPosition) {
        return new Property<>(buffer, startPosition, Short.BYTES, Integer.class, typed(StandardMarshaller.UNSIGNED_WORD_LE));
    }

    /**
     * not ported.
     */
    public static Property<Integer> int32(ByteBuffer buffer, int startPosition) {
        return new Property<>(buffer, startPosition, Integer.BYTES, Integer.class, typed(StandardMarshaller.INT_LE));
    }

    /**
     * not ported.
     */
    public static Property<byte[]> bytes(ByteBuffer buffer, int startPosition, int size) {
        return new Property<>(buffer, startPosition, size, byte[].class, typed(StandardMarshaller.BYTE_ARRAY));
    }

    /**
     * not ported.
     */
    public static Property<byte[]> bytes(ByteBuffer buffer, int startPosition, IntSupplier sizeSupplier) {
        return new Property<>(buffer, startPosition, sizeSupplier, byte[].class, typed(StandardMarshaller.BYTE_ARRAY));
    }

    /**
     * not ported.
     */
    public static Property<String> fixedCString(ByteBuffer buffer, int startPosition, int size) {
        return new Property<>(buffer, startPosition, size, String.class, typed(StandardMarshaller.FIXED_C_STRING_ISO_8859_1));
    }

    /**
     * not ported.
     */
    public int startPosition() {
        return startPosition;
    }

    /**
     * not ported.
     */
    public int size() {
        int size = sizeSupplier.getAsInt();
        if (size < 0) {
            throw new IllegalStateException("Negative native property size " + size);
        }
        return size;
    }

    /**
     * not ported.
     */
    public Class<T> type() {
        return type;
    }

    /**
     * not ported.
     */
    public T get() {
        return marshaller.get(buffer, startPosition, size());
    }

    /**
     * not ported.
     */
    public void set(T value) {
        marshaller.put(buffer, startPosition, size(), value);
    }

    /**
     * not ported.
     */
    public boolean touches(int nativeObjectOffset, int length) {
        return CGameAction.nativeRangeTouches(nativeObjectOffset, length, startPosition, size());
    }

    /**
     * not ported.
     */
    public byte byteAt(int relativeOffset) {
        validateRelativeRange(relativeOffset, Byte.BYTES);
        return buffer.get(startPosition + relativeOffset);
    }

    /**
     * not ported.
     */
    public int unsignedByteAt(int relativeOffset) {
        return Byte.toUnsignedInt(byteAt(relativeOffset));
    }

    /**
     * not ported.
     */
    public void setByteAt(int relativeOffset, int value) {
        validateRelativeRange(relativeOffset, Byte.BYTES);
        buffer.put(startPosition + relativeOffset, (byte) value);
    }

    /**
     * not ported.
     */
    public byte byteAtIndex(int index) {
        return byteAt(indexedRelativeOffset(index, Byte.BYTES));
    }

    /**
     * not ported.
     */
    public int unsignedByteAtIndex(int index) {
        return unsignedByteAt(indexedRelativeOffset(index, Byte.BYTES));
    }

    /**
     * not ported.
     */
    public void setByteAtIndex(int index, int value) {
        setByteAt(indexedRelativeOffset(index, Byte.BYTES), value);
    }

    /**
     * not ported.
     */
    public int unsignedWordAt(int relativeOffset) {
        validateRelativeRange(relativeOffset, Short.BYTES);
        return Short.toUnsignedInt(buffer.getShort(startPosition + relativeOffset));
    }

    /**
     * not ported.
     */
    public void setUnsignedWordAt(int relativeOffset, int value) {
        validateRelativeRange(relativeOffset, Short.BYTES);
        buffer.putShort(startPosition + relativeOffset, (short) value);
    }

    /**
     * not ported.
     */
    public int unsignedWordAtIndex(int index) {
        return unsignedWordAt(indexedRelativeOffset(index, Short.BYTES));
    }

    /**
     * not ported.
     */
    public void setUnsignedWordAtIndex(int index, int value) {
        setUnsignedWordAt(indexedRelativeOffset(index, Short.BYTES), value);
    }

    /**
     * not ported.
     */
    public int intAt(int relativeOffset) {
        validateRelativeRange(relativeOffset, Integer.BYTES);
        return buffer.getInt(startPosition + relativeOffset);
    }

    /**
     * not ported.
     */
    public void setIntAt(int relativeOffset, int value) {
        validateRelativeRange(relativeOffset, Integer.BYTES);
        buffer.putInt(startPosition + relativeOffset, value);
    }

    /**
     * not ported.
     */
    public int intAtIndex(int index) {
        return intAt(indexedRelativeOffset(index, Integer.BYTES));
    }

    /**
     * not ported.
     */
    public void setIntAtIndex(int index, int value) {
        setIntAt(indexedRelativeOffset(index, Integer.BYTES), value);
    }

    /**
     * not ported.
     */
    public void fill(byte value) {
        fillAt(0, size(), value);
    }

    /**
     * not ported.
     */
    public void fillAt(int relativeOffset, int length, byte value) {
        validateRelativeRange(relativeOffset, length);
        for (int i = 0; i < length; i++) {
            buffer.put(startPosition + relativeOffset + i, value);
        }
    }

    /**
     * not ported.
     */
    public void setBytesAt(int relativeOffset, byte[] source, int sourceOffset, int length) {
        if (source == null || sourceOffset < 0 || length < 0 || sourceOffset + length > source.length) {
            throw new IllegalArgumentException("Invalid property byte source");
        }
        validateRelativeRange(relativeOffset, length);
        ByteBuffer duplicate = buffer.duplicate();
        duplicate.position(startPosition + relativeOffset);
        duplicate.put(source, sourceOffset, length);
    }

    /**
     * not ported.
     */
    public ByteBuffer buffer() {
        return bufferAt(0, size());
    }

    /**
     * not ported.
     */
    public ByteBuffer bufferAt(int relativeOffset, int length) {
        validateRelativeRange(relativeOffset, length);
        ByteBuffer duplicate = buffer.duplicate();
        duplicate.position(startPosition + relativeOffset);
        duplicate.limit(startPosition + relativeOffset + length);
        return duplicate.slice().order(buffer.order());
    }

    /**
     * not ported.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Marshaller<T> typed(StandardMarshaller marshaller) {
        return (Marshaller) marshaller;
    }

    /**
     * not ported.
     */
    private static IntSupplier fixedSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative native property size " + size);
        }
        return () -> size;
    }

    /**
     * not ported.
     */
    private static <T> Marshaller<T> defaultMarshaller(int size, Class<T> type) {
        if (type == Integer.class && size == Byte.BYTES) {
            return typed(StandardMarshaller.UNSIGNED_BYTE);
        }
        if (type == Integer.class && size == Short.BYTES) {
            return typed(StandardMarshaller.UNSIGNED_WORD_LE);
        }
        if (type == Integer.class && size == Integer.BYTES) {
            return typed(StandardMarshaller.INT_LE);
        }
        if (type == byte[].class) {
            return typed(StandardMarshaller.BYTE_ARRAY);
        }
        if (type == String.class) {
            return typed(StandardMarshaller.FIXED_C_STRING_ISO_8859_1);
        }
        throw new IllegalArgumentException("No default marshaller for " + type.getName() + "[" + size + "]");
    }

    /**
     * not ported.
     */
    private void validateRelativeRange(int relativeOffset, int length) {
        if (relativeOffset < 0 || length < 0 || relativeOffset + length > size()) {
            throw new IndexOutOfBoundsException("Native property relative range 0x"
                    + Integer.toHexString(relativeOffset) + "..0x"
                    + Integer.toHexString(relativeOffset + length));
        }
    }

    /**
     * not ported.
     */
    private int indexedRelativeOffset(int index, int elementSize) {
        if (index < 0 || elementSize <= 0) {
            throw new IndexOutOfBoundsException("Native property element index " + index);
        }
        int relativeOffset = Math.multiplyExact(index, elementSize);
        validateRelativeRange(relativeOffset, elementSize);
        return relativeOffset;
    }

    /**
     * not ported.
     */
    @Override
    public String toString() {
        return get().toString();
    }
}
