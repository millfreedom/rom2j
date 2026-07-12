package ua.millfreedom.rom2;

import java.nio.ByteBuffer;
import java.util.Objects;

public final class CString {
    private final int byteCapacity;
    private String value;

    // not ported.
    public CString(byte[] bytes) {
        byteCapacity = bytes == null ? 0 : bytes.length;
        set(bytes);
    }

    // not ported.
    public CString(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must be >= 0");
        }
        byteCapacity = size;
        value = "";
    }

    // not ported.
    public void set(byte[] bytes) {
        if (bytes == null) {
            value = "";
            return;
        }
        int length = 0;
        int limit = Math.min(byteCapacity, bytes.length);
        while (length < limit && bytes[length] != 0) {
            length++;
        }
        value = new String(bytes, 0, length, GameCharsets.GAME_TEXT);
    }

    // not ported. Stores Java text as Unicode; fixed-field encoding happens only in write(ByteBuffer).
    public void set(String value) {
        String text = value == null ? "" : value;
        int codePointCount = text.codePointCount(0, text.length());
        if (codePointCount > byteCapacity) {
            text = text.substring(0, text.offsetByCodePoints(0, byteCapacity));
        }
        this.value = text;
    }

    // not ported.
    public void clear() {
        value = "";
    }

    // not ported.
    public CString read(ByteBuffer bb) {
        Objects.requireNonNull(bb, "bb");
        byte[] bytes = new byte[byteCapacity];
        int copy = Math.min(byteCapacity, bb.remaining());
        if (copy > 0) {
            bb.get(bytes, 0, copy);
        }
        set(bytes);
        return this;
    }

    // not ported.
    public void write(ByteBuffer bb) {
        Objects.requireNonNull(bb, "bb");
        byte[] bytes = new byte[byteCapacity];
        byte[] encoded = value.getBytes(GameCharsets.GAME_TEXT);
        System.arraycopy(encoded, 0, bytes, 0, Math.min(bytes.length, encoded.length));
        bb.put(bytes);
    }

    // not ported.
    public int length() {
        return byteCapacity;
    }

    @Override
    // not ported.
    public String toString() {
        return value;
    }
}
