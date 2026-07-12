package ua.millfreedom.rom2.CFile;


import lombok.SneakyThrows;
import ua.millfreedom.rom2.GameCharsets;
import ua.millfreedom.rom2.data.RWAdaptor;
import ua.millfreedom.rom2.res.Resources;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

public final class CFile implements AutoCloseable, RWAdaptor {
    public static final int BUFFER_SIZE = 4096;

    //0x0c
    public String m_strFileName = "";

    private final Charset charset;
    private final FileChannel fileChannel;
    private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    private final ByteBuffer b1 = ByteBuffer.allocate(1);
    private final ByteBuffer b2 = ByteBuffer.allocate(2);
    private final ByteBuffer b4 = ByteBuffer.allocate(4);
    private final ByteBuffer b8 = ByteBuffer.allocate(8);

    /**
     * not ported. Java resource-path constructor.
     */
    public CFile(String fileName) {
        this(fileName, GameCharsets.GAME_TEXT);
    }

    /**
     * not ported. Java resource-path constructor.
     */
    @SneakyThrows
    public CFile(String fileName, Charset charset) {
        this.charset = Objects.requireNonNull(charset);
        setFilePath(fileName);
        this.fileChannel = FileChannel.open(Resources.getPath(fileName), READ, WRITE);
    }

    /**
     * not ported. Java filesystem-path constructor.
     */
    public CFile(Path path) throws IOException {
        this(path, GameCharsets.GAME_TEXT);
    }

    /**
     * not ported. Java filesystem-path constructor.
     */
    public CFile(Path path, Charset charset) throws IOException {
        this.charset = Objects.requireNonNull(charset);
        Path resolvedPath = Objects.requireNonNull(path);
        setFilePath(resolvedPath.toString());
        this.fileChannel = FileChannel.open(resolvedPath, READ);
    }

    /**
     * Native: CFile::SetFilePath @004E4820.
     * Fully ported.
     */
    public void setFilePath(String fileName) {
        m_strFileName = fileName;
    }

    @Override
    public void close() throws IOException {
        fileChannel.close();
    }

    public Charset getCharset() {
        return charset;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public CFile setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
        return this;
    }

    private ByteBuffer prepareToRead(ByteBuffer buffer) throws IOException {
        if (fileChannel.read(buffer.clear().order(byteOrder)) != -1) {
            return buffer.flip();
        } else {
            throw new EOFException();
        }
    }

    private int writeBuffer(ByteBuffer buffer, Consumer<ByteBuffer> operation) throws IOException {
        operation.accept(buffer.clear().order(byteOrder));
        return fileChannel.write(buffer.flip());
    }

    // ---- primitives ----
    public int readInt() throws IOException {
        return prepareToRead(b4).getInt();
    }

    public void writeInt(int v) throws IOException {
        writeBuffer(b4, b -> b.putInt(v));
    }

    public short readShort() throws IOException {
        return prepareToRead(b2).getShort();
    }

    public int readUShort() throws IOException {
        return Short.toUnsignedInt(prepareToRead(b2).getShort());
    }

    public void writeShort(int v) throws IOException {
        writeBuffer(b2, b -> b.putShort((short) v));
    }

    public byte readByte() throws IOException {
        return prepareToRead(b1).get();
    }

    public void writeByte(int v) throws IOException {
        writeBuffer(b1, b -> b.put((byte) v));
    }


    public long readLong() throws IOException {
        return prepareToRead(b8).getLong();
    }

    public void writeLong(long v) throws IOException {
        writeBuffer(b8, b -> b.putLong(v));
    }

    public double readDouble() throws IOException {
        return prepareToRead(b8).getDouble();
    }

    public void writeDouble(double v) throws IOException {
        writeBuffer(b8, b -> b.putDouble(v));
    }

    public boolean readBool() throws IOException {
        return readInt() != 0;
    }

    public void writeBool(boolean v) throws IOException {
        writeInt(v ? 1 : 0);
    }

    public ByteBuffer readRemaining() throws IOException {
        int remaining = Math.toIntExact(fileChannel.size() - fileChannel.position());
        return readBytes(remaining);
    }

    public ByteBuffer readBytes(int size) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        while (buffer.hasRemaining()) {
            int n = fileChannel.read(buffer);
            if (n < 0) break;
        }
        return buffer.flip();
    }

    public ByteBuffer readBytes(int offset, int size) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        while (buffer.hasRemaining()) {
            int n = fileChannel.read(buffer, offset);
            if (n < 0) break;
        }
        return buffer.flip();
    }

    public void writeBytes(byte[] b) throws IOException {
        int written = 0;
        while (written < b.length) {
            int wb = fileChannel.write(ByteBuffer.wrap(b));
            if (wb < 0) break;
            written += wb;
        }
    }


    @Override
    @SneakyThrows
    public long position() {
        return fileChannel.position();
    }

    public long seek(long pos, SeekType type) throws IOException {
        final long base = switch (type) {
            case BEGIN -> 0L;
            case CURRENT -> fileChannel.position();
            case END -> fileChannel.size();
        };
        final long newPos = base + pos; // pos may be negative for current/end
        if (newPos < 0) {
            throw new IllegalArgumentException("seek resulted in negative position: " + newPos);
        }

        fileChannel.position(newPos);
        return newPos;
    }

    public long getCurrentPosition() throws IOException {
        return fileChannel.position();
    }

    public long getSize() throws IOException {
        return fileChannel.size();
    }

    public static ByteBuffer readAt(String path, long offset, int size) throws Exception {
        try (CFile f = new CFile(path)) {
            f.seek(offset, SeekType.BEGIN);
            return f.readBytes(size);
        }
    }



    public enum SeekType {
        BEGIN, CURRENT, END;
    }
}
