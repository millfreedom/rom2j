package ua.millfreedom.rom2.CFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* =======================
 * Little-endian writer
 * ======================= */
public final class LEWriter implements AutoCloseable,EndianWriter {
    private final OutputStream out;
    private final byte[] tmp8 = new byte[8];

    public LEWriter(OutputStream out) {
        this.out = out;
    }

    public void writeU8(int v) throws IOException {
        out.write(v & 0xFF);
    }

    public void writeI16(int v) throws IOException {
        writeU8(v);
        writeU8(v >>> 8);
    }

    public void writeI32(int v) throws IOException {
        writeU8(v);
        writeU8(v >>> 8);
        writeU8(v >>> 16);
        writeU8(v >>> 24);
    }

    public void writeI64(long v) throws IOException {
        ByteBuffer.wrap(tmp8).order(ByteOrder.LITTLE_ENDIAN).putLong(0, v);
        out.write(tmp8, 0, 8);
    }

    public void writeBytes(byte[] b) throws IOException {
        out.write(b);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
