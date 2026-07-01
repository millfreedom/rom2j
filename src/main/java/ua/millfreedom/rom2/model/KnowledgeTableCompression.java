package ua.millfreedom.rom2.model;

import java.io.ByteArrayOutputStream;

/**
 * Native support for player knowledge table compression.
 */
public final class KnowledgeTableCompression {
    private static final int KNOWLEDGE_RUN_MAX_SIZE = 0x7F;

    /**
     * not ported.
     */
    private KnowledgeTableCompression() {
    }

    /**
     * Native support extracted from compressKnowledgeTable @0053C140.
     * Fully ported.
     */
    public static byte[] compress(byte[] knowledgeTable) {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream(knowledgeTable.length);
        writeLengthHeader(compressed, knowledgeTable.length);
        int offset = 0;
        while (offset < knowledgeTable.length) {
            if (offset + 1 < knowledgeTable.length && knowledgeTable[offset] == knowledgeTable[offset + 1]) {
                offset += appendRepeatedKnowledgeRun(knowledgeTable, offset, compressed);
            } else {
                offset += appendLiteralKnowledgeRun(knowledgeTable, offset, compressed);
            }
        }
        return compressed.toByteArray();
    }

    /**
     * Native support extracted from compressKnowledgeTable @0053C140 little-endian original-size header write.
     * Fully ported.
     */
    private static void writeLengthHeader(ByteArrayOutputStream compressed, int knowledgeTableSize) {
        compressed.write(knowledgeTableSize & 0xFF);
        compressed.write((knowledgeTableSize >>> 8) & 0xFF);
        compressed.write((knowledgeTableSize >>> 16) & 0xFF);
        compressed.write((knowledgeTableSize >>> 24) & 0xFF);
    }

    /**
     * Native support extracted from appendRepeatedKnowledgeRun @0053C1F0.
     * Fully ported.
     */
    private static int appendRepeatedKnowledgeRun(byte[] knowledgeTable, int offset, ByteArrayOutputStream compressed) {
        int runLength = 1;
        while (runLength < KNOWLEDGE_RUN_MAX_SIZE
                && offset + runLength < knowledgeTable.length
                && knowledgeTable[offset + runLength - 1] == knowledgeTable[offset + runLength]) {
            runLength++;
        }
        compressed.write(runLength | 0x80);
        compressed.write(knowledgeTable[offset + runLength - 1]);
        return runLength;
    }

    /**
     * Native support extracted from appendLiteralKnowledgeRun @0053C2B0.
     * Fully ported.
     */
    private static int appendLiteralKnowledgeRun(byte[] knowledgeTable, int offset, ByteArrayOutputStream compressed) {
        int runLength = 1;
        while (runLength < KNOWLEDGE_RUN_MAX_SIZE
                && offset + runLength < knowledgeTable.length
                && knowledgeTable[offset + runLength - 1] != knowledgeTable[offset + runLength]) {
            runLength++;
        }
        if (offset + runLength == knowledgeTable.length) {
            runLength++;
        }
        int literalLength = runLength - 1;
        compressed.write(literalLength);
        compressed.write(knowledgeTable, offset, literalLength);
        return literalLength;
    }
}
