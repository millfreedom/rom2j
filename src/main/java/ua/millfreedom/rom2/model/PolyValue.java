package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

import java.io.IOException;

/**
 * 32-bit packed value with byte/word accessors.
 * Byte/word numbering is little-endian:
 * - B1: bits 0..7
 * - B2: bits 8..15
 * - B3: bits 16..23
 * - B4: bits 24..31
 * - S1: bits 0..15
 * - S2: bits 16..31
 */
public class PolyValue implements MfcSerializable {
    public int full;

    // not ported.
    public int getFull() {
        return full;
    }

    // not ported.
    public void setFull(int full) {
        this.full = full;
    }

    // not ported.
    public int getS1() {
        return full & 0xFFFF;
    }

    // not ported.
    public int getS1Signed() {
        return (short) getS1();
    }

    // not ported.
    public void setS1(int value) {
        full = (full & 0xFFFF0000) | (value & 0xFFFF);
    }

    // not ported.
    public int getS2() {
        return (full >>> 16) & 0xFFFF;
    }

    // not ported.
    public int getS2Signed() {
        return (short) getS2();
    }

    // not ported.
    public void setS2(int value) {
        full = (full & 0x0000FFFF) | ((value & 0xFFFF) << 16);
    }

    // not ported.
    public int getB1() {
        return full & 0xFF;
    }

    // not ported.
    public void setB1(int value) {
        full = (full & 0xFFFFFF00) | (value & 0xFF);
    }

    // not ported.
    public int getB2() {
        return (full >>> 8) & 0xFF;
    }

    // not ported.
    public void setB2(int value) {
        full = (full & 0xFFFF00FF) | ((value & 0xFF) << 8);
    }

    // not ported.
    public int getB3() {
        return (full >>> 16) & 0xFF;
    }

    // not ported.
    public void setB3(int value) {
        full = (full & 0xFF00FFFF) | ((value & 0xFF) << 16);
    }

    // not ported.
    public int getB4() {
        return (full >>> 24) & 0xFF;
    }

    // not ported.
    public void setB4(int value) {
        full = (full & 0x00FFFFFF) | ((value & 0xFF) << 24);
    }

    @Override
    // not ported.
    public void serialize(CArchive ar) throws IOException {
        if (!ar.isStoring()) {
            full = ar.readInt();
        } else {
            ar.writeInt(full);
        }
    }
}

