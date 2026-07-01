package ua.millfreedom.rom2.CFile;

import java.io.IOException;

public interface EndianWriter {
    void writeU8(int v) throws IOException;

    void writeI16(int v) throws IOException;

    void writeI32(int v) throws IOException;

    void writeI64(long v) throws IOException;

    void writeBytes(byte[] b) throws IOException;
}
