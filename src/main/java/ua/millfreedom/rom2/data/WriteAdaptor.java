package ua.millfreedom.rom2.data;

import java.io.IOException;

public interface WriteAdaptor extends Ordered, Positioned, WithCharset {

    void writeDouble(double v) throws IOException;

    void writeBytes(byte[] b) throws IOException;

    void writeBool(boolean v) throws IOException;

    void writeShort(int v) throws IOException;

    void writeByte(int v) throws IOException;

    void writeInt(int v) throws IOException;

}
