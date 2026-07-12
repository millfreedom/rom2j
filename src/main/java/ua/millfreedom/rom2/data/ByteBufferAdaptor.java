package ua.millfreedom.rom2.data;

import ua.millfreedom.rom2.GameCharsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Objects;

public final class ByteBufferAdaptor implements RWAdaptor {
    private static final int DEFAULT_WRITE_CAPACITY = 256;

    private ByteBuffer bb;
    private final AccessMode accessMode;
    private final ByteArrayOutputStream output;
    private boolean closed;

    public enum AccessMode {
        READ,
        WRITE,
        READ_WRITE
    }

    /**
     * not ported.
     */
    public ByteBufferAdaptor(ByteBuffer bb) {
        this(bb, AccessMode.READ, null);
    }

    /**
     * not ported.
     */
    public static ByteBufferAdaptor forReading(ByteBuffer source) {
        return new ByteBufferAdaptor(source);
    }

    /**
     * not ported.
     */
    public static ByteBufferAdaptor forReading(byte[] source) {
        return forReading(source, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * not ported.
     */
    public static ByteBufferAdaptor forReading(byte[] source, ByteOrder order) {
        ByteBufferAdaptor adaptor = new ByteBufferAdaptor(ByteBuffer.wrap(Objects.requireNonNull(source)));
        adaptor.setByteOrder(order);
        return adaptor;
    }

    /**
     * not ported.
     */
    public static ByteBufferAdaptor forWriting(ByteArrayOutputStream output) {
        return forWriting(output, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * not ported.
     */
    public static ByteBufferAdaptor forWriting(ByteArrayOutputStream output, ByteOrder order) {
        return new ByteBufferAdaptor(
                ByteBuffer.allocate(DEFAULT_WRITE_CAPACITY).order(Objects.requireNonNull(order)),
                AccessMode.WRITE,
                Objects.requireNonNull(output)
        );
    }

    /**
     * not ported.
     */
    public static ByteBufferAdaptor forWriting(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity < 0");
        }
        return new ByteBufferAdaptor(
                ByteBuffer.allocate(Math.max(initialCapacity, 1)).order(ByteOrder.LITTLE_ENDIAN),
                AccessMode.WRITE,
                null
        );
    }

    /**
     * not ported.
     */
    public static ByteBufferAdaptor forReadWrite(ByteBuffer source) {
        return new ByteBufferAdaptor(source, AccessMode.READ_WRITE, null);
    }

    /**
     * not ported.
     */
    @Override
    public double readDouble() throws IOException {
        requireReadable("readDouble");
        return bb.getDouble();
    }

    /**
     * not ported.
     */
    @Override
    public ByteBuffer readBytes(int count) throws IOException {
        requireReadable("readBytes");
        byte[] buff = new byte[count];
        bb.get(buff);
        return ByteBuffer.wrap(buff).order(bb.order());
    }

    /**
     * not ported.
     */
    @Override
    public boolean readBool() throws IOException {
        return readInt() != 0;
    }

    /**
     * not ported.
     */
    @Override
    public byte readByte() throws IOException {
        requireReadable("readByte");
        return bb.get();
    }

    /**
     * not ported.
     */
    @Override
    public int readUShort() throws IOException {
        requireReadable("readUShort");
        return Short.toUnsignedInt(bb.getShort());
    }

    /**
     * not ported.
     */
    @Override
    public short readShort() throws IOException {
        requireReadable("readShort");
        return bb.getShort();
    }

    /**
     * not ported.
     */
    @Override
    public int readInt() throws IOException {
        requireReadable("readInt");
        return bb.getInt();
    }

    /**
     * not ported.
     */
    @Override
    public ByteBufferAdaptor setByteOrder(ByteOrder bo) {
        bb.order(Objects.requireNonNull(bo));
        return this;
    }

    /**
     * not ported.
     */
    @Override
    public ByteOrder getByteOrder() {
        return bb.order();
    }

    /**
     * not ported.
     */
    @Override
    public Charset getCharset() {
        return GameCharsets.GAME_TEXT;
    }

    /**
     * not ported.
     */
    @Override
    public void writeDouble(double v) throws IOException {
        requireWritable("writeDouble");
        ensureWritable(Double.BYTES);
        bb.putDouble(v);
    }

    /**
     * not ported.
     */
    @Override
    public void writeBytes(byte[] b) throws IOException {
        requireWritable("writeBytes");
        byte[] bytes = Objects.requireNonNull(b);
        ensureWritable(bytes.length);
        bb.put(bytes);
    }

    /**
     * not ported.
     */
    @Override
    public void writeBool(boolean v) throws IOException {
        writeInt(v ? 1 : 0);
    }

    /**
     * not ported.
     */
    @Override
    public void writeShort(int v) throws IOException {
        requireWritable("writeShort");
        ensureWritable(Short.BYTES);
        bb.putShort((short) v);
    }

    /**
     * not ported.
     */
    @Override
    public void writeByte(int v) throws IOException {
        requireWritable("writeByte");
        ensureWritable(Byte.BYTES);
        bb.put((byte) v);
    }

    /**
     * not ported.
     */
    @Override
    public void writeInt(int v) throws IOException {
        requireWritable("writeInt");
        ensureWritable(Integer.BYTES);
        bb.putInt(v);
    }

    /**
     * not ported.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (output != null) {
            output.writeBytes(toByteArray());
        }
    }

    /**
     * not ported.
     */
    @Override
    public long position() {
        return bb.position();
    }

    /**
     * not ported.
     */
    public byte[] toByteArray() {
        ByteBuffer duplicate = bb.duplicate();
        int length = duplicate.position();
        duplicate.position(0);
        duplicate.limit(length);
        byte[] result = new byte[length];
        duplicate.get(result);
        return result;
    }

    /**
     * not ported.
     */
    private ByteBufferAdaptor(ByteBuffer bb, AccessMode accessMode, ByteArrayOutputStream output) {
        this.bb = Objects.requireNonNull(bb).duplicate().order(bb.order());
        this.accessMode = Objects.requireNonNull(accessMode);
        this.output = output;
    }

    /**
     * not ported.
     */
    private void ensureWritable(int byteCount) {
        if (bb.remaining() >= byteCount) {
            return;
        }
        int requiredCapacity = bb.position() + byteCount;
        int nextCapacity = Math.max(bb.capacity() * 2, requiredCapacity);
        ByteBuffer grown = ByteBuffer.allocate(nextCapacity).order(bb.order());
        bb.flip();
        grown.put(bb);
        bb = grown;
    }

    /**
     * not ported.
     */
    private void requireReadable(String operation) throws IOException {
        requireOpen(operation);
        if (accessMode == AccessMode.WRITE) {
            throw new IOException(operation + " called on writing memory adaptor");
        }
    }

    /**
     * not ported.
     */
    private void requireWritable(String operation) throws IOException {
        requireOpen(operation);
        if (accessMode == AccessMode.READ) {
            throw new IOException(operation + " called on reading memory adaptor");
        }
    }

    /**
     * not ported.
     */
    private void requireOpen(String operation) throws IOException {
        if (closed) {
            throw new IOException(operation + " called on closed memory adaptor");
        }
    }
}
