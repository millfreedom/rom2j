package ua.millfreedom.rom2.model.world.scenario;

import java.nio.ByteBuffer;

/**
 * ALM section header (0x14 bytes) consumed by ScenarioDescriptor::load @00534ad4.
 */
public final class ScenarioSectionHeader {
    public static final int BYTE_SIZE = 0x14;

    //0x00
    public int type;
    //0x04
    public int headerSize;
    //0x08
    public int dataSize;
    //0x0C
    public int id;
    //0x10
    public int version;

    /**
     * not ported. Java zero-initialized holder used by SectionHeader::Load @00534A9D.
     */
    public ScenarioSectionHeader() {
    }

    /**
     * not ported. Java value constructor for records loaded by SectionHeader::Load @00534A9D.
     */
    public ScenarioSectionHeader(int type, int headerSize, int dataSize, int id, int version) {
        this.type = type;
        this.headerSize = headerSize;
        this.dataSize = dataSize;
        this.id = id;
        this.version = version;
    }

    /**
     * Native: SectionHeader::Load @00534A9D.
     * Fully ported.
     */
    public void load(ByteBuffer bb, ScenarioFileHeader scenarioFileHdr) {
        if (scenarioFileHdr.version != 1000) {
            readCurrentFormat(bb);
        }
    }

    /**
     * Native support extracted from SectionHeader::Load @00534A9D.
     * Fully ported.
     */
    public static ScenarioSectionHeader read(ByteBuffer bb, ScenarioFileHeader scenarioFileHdr) {
        ScenarioSectionHeader header = new ScenarioSectionHeader();
        header.load(bb, scenarioFileHdr);
        return header;
    }

    /**
     * Native support extracted from SectionHeader::Load @00534A9D for callers that already require section headers.
     * Fully ported.
     */
    public static ScenarioSectionHeader read(ByteBuffer bb) {
        ScenarioSectionHeader header = new ScenarioSectionHeader();
        header.readCurrentFormat(bb);
        return header;
    }

    /**
     * Native support extracted from SectionHeader::Load @00534A9D.
     * Fully ported.
     */
    private void readCurrentFormat(ByteBuffer bb) {
        int type = bb.getInt();
        int headerSize = bb.getInt();
        int dataSize = bb.getInt();
        int id = bb.getInt();
        int version = bb.getInt();
        this.type = type;
        this.headerSize = headerSize;
        this.dataSize = dataSize;
        this.id = id;
        this.version = version;
    }
}
