package ua.millfreedom.rom2.CFile;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* =======================
 * Big-endian reader
 * ======================= */
public final class BEReader implements AutoCloseable, EndianReader {
    private final InputStream in;
    private final byte[] tmp8 = new byte[8];

    public BEReader(InputStream in) {
        this.in = in;
    }

    public int readU8() throws IOException {
        int b = in.read();
        if (b < 0) throw new EOFException();
        return b;
    }

    public short readI16() throws IOException {
        int b0 = readU8(), b1 = readU8();
        return (short) ((b0 << 8) | b1);
    }

    public int readU16() throws IOException {
        int b0 = readU8(), b1 = readU8();
        return ((b0 << 8) | b1) & 0xFFFF;
    }

    public int readI32() throws IOException {
        int b0 = readU8(), b1 = readU8(), b2 = readU8(), b3 = readU8();
        return (b0 << 24) | (b1 << 16) | (b2 << 8) | (b3);
    }

    public long readI64() throws IOException {
        readFully(tmp8, 0, 8);
        return ByteBuffer.wrap(tmp8).order(ByteOrder.BIG_ENDIAN).getLong(0);
    }

    public void readFully(byte[] buf, int off, int len) throws IOException {
        while (len > 0) {
            int n = in.read(buf, off, len);
            if (n < 0) throw new EOFException();
            off += n;
            len -= n;
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
