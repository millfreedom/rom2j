package ua.millfreedom.rom2.data;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ReadAdaptor extends Ordered, Positioned, WithCharset {

    double readDouble() throws IOException;

    ByteBuffer readBytes(int count) throws IOException;

    boolean readBool() throws IOException;

    byte readByte() throws IOException;

    int readUShort() throws IOException;

    short readShort() throws IOException;

    int readInt() throws IOException;


}
