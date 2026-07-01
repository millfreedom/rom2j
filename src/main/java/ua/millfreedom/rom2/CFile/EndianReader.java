package ua.millfreedom.rom2.CFile;

import java.io.IOException;

public interface EndianReader {
    int readU8() throws IOException;

    short readI16() throws IOException;

    int readU16() throws IOException;

    int readI32() throws IOException;

    long readI64() throws IOException;

    void readFully(byte[] buf, int off, int len) throws IOException;
}
