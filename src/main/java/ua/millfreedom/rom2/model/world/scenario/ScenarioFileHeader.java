package ua.millfreedom.rom2.model.world.scenario;

import ua.millfreedom.rom2.CString;

import java.nio.ByteBuffer;

/**
 * ALM file header (0x14 bytes) consumed by ScenarioDescriptor::load @00534ad4.
 */
public final class ScenarioFileHeader {
    public static final int BYTE_SIZE = 0x14;

    //0x00
    public final CString signature;
    //0x04
    public final int headerSize;
    //0x08
    public final int dataSize;
    //0x0C
    public final int sectionCount;
    //0x10
    public final int version;

    /**
     * not ported. Java value constructor for records loaded by ScenarioFileHeader::Load @00534ABE.
     */
    public ScenarioFileHeader(CString signature, int headerSize, int dataSize, int sectionCount, int version) {
        this.signature = signature;
        this.headerSize = headerSize;
        this.dataSize = dataSize;
        this.sectionCount = sectionCount;
        this.version = version;
    }

    /**
     * Native: ScenarioFileHeader::Load @00534ABE.
     * Fully ported.
     */
    public static ScenarioFileHeader read(ByteBuffer bb) {
        byte[] signatureBytes = new byte[4];
        bb.get(signatureBytes);
        CString signature = new CString(signatureBytes);
        int headerSize = bb.getInt();
        int dataSize = bb.getInt();
        int sectionCount = bb.getInt();
        int version = bb.getInt();
        return new ScenarioFileHeader(signature, headerSize, dataSize, sectionCount, version);
    }


    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 signature check.
     * Fully ported.
     */
    public boolean isM7R() {
        return signature.toString().equals("M7R");
    }
}
