package ua.millfreedom.rom2;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public final class CString {
    private final byte[] byteString;
    private String cachedString;

    // not ported.
    public CString(byte[] bytes) {
        byteString = bytes == null ? new byte[0] : bytes.clone();
    }

    // not ported.
    public CString(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must be >= 0");
        }
        byteString = new byte[size];
    }

    // not ported.
    public void set(byte[] bytes) {
        clear();
        if (bytes != null && byteString.length > 0) {
            int copy = Math.min(byteString.length, bytes.length);
            System.arraycopy(bytes, 0, byteString, 0, copy);
        }
    }

    // not ported.
    public void clear() {
        Arrays.fill(byteString, (byte) 0);
        cachedString = null;
    }

    // not ported.
    public CString read(ByteBuffer bb) {
        Objects.requireNonNull(bb, "bb");
        clear();
        int copy = Math.min(byteString.length, bb.remaining());
        if (copy > 0) {
            bb.get(byteString, 0, copy);
        }
        return this;
    }

    // not ported.
    public void write(ByteBuffer bb) {
        Objects.requireNonNull(bb, "bb");
        bb.put(byteString);
    }

    // not ported.
    public int length() {
        return byteString.length;
    }

    @Override
    // not ported.
    public String toString() {
        if (cachedString == null) {
            int len = 0;
            while (len < byteString.length && byteString[len] != 0) {
                len++;
            }
            cachedString = new String(byteString, 0, len, StandardCharsets.ISO_8859_1);
        }
        return cachedString;
    }
}
