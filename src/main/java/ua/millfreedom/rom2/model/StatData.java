package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Native size: 0x16
 * +0x00 short Defence
 * +0x02 short Absorbtion
 * +0x04 short[6] protections
 * +0x10 byte[6]  m_bModifiers
 */
public class StatData implements MfcSerializable {
    public static final int BYTE_SIZE = 0x16;

    //0x00
    public short defence;
    //0x02
    public short absorbtion;
    //0x04
    public final short[] protections = new short[6];
    //0x10
    public final byte[] m_bModifiers = new byte[6];

    /**
     * Native: StatData::New @005155C7 and StatData::Init @005155DD.
     * Fully ported.
     */
    public StatData() {
    }

    /**
     * Native: StatData::StatData @005155F8.
     * Fully ported.
     */
    public StatData(StatData from) {
        assign(from);
    }

    /**
     * Native: StatData::Assign @005156BF.
     * Fully ported.
     */
    public StatData assign(StatData from) {
        for (int i = 0; i < 6; i++) {
            protections[i] = from.protections[i];
        }
        for (int i = 0; i < 6; i++) {
            m_bModifiers[i] = from.m_bModifiers[i];
        }
        defence = from.defence;
        absorbtion = from.absorbtion;
        return from;
    }

    /**
     * Native: StatData::Add @00515617.
     * Fully ported.
     */
    public void add(StatData from) {
        for (int i = 0; i < 6; i++) {
            protections[i] = (short) (protections[i] + from.protections[i]);
        }
        for (int i = 0; i < 6; i++) {
            m_bModifiers[i] = (byte) (m_bModifiers[i] + from.m_bModifiers[i]);
        }
        defence = (short) (defence + from.defence);
        absorbtion = (short) (absorbtion + from.absorbtion);
    }

    /**
     * Native: StatData::Subtract @00515746.
     * Fully ported.
     */
    public void subtract(StatData from) {
        for (int i = 0; i < 6; i++) {
            protections[i] = (short) (protections[i] - from.protections[i]);
        }
        for (int i = 0; i < 6; i++) {
            m_bModifiers[i] = (byte) (m_bModifiers[i] - from.m_bModifiers[i]);
        }
        defence = (short) (defence - from.defence);
        absorbtion = (short) (absorbtion - from.absorbtion);
    }

    /**
     * Native: StatData::Init @005155DD.
     * Fully ported.
     */
    public void init() {
        defence = 0;
        absorbtion = 0;
        Arrays.fill(protections, (short) 0);
        Arrays.fill(m_bModifiers, (byte) 0);
    }

    /**
     * Native: StatData::Serialize @005157EE.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (!ar.isStoring()) {
            fromBytes(ar.readBytes(BYTE_SIZE));
        } else {
            ar.writeBytes(toBytes());
        }
    }

    /**
     * Native support extracted from StatData::Serialize @005157EE raw read.
     * Fully ported.
     */
    void fromBytes(byte[] raw) {
        if (raw.length != BYTE_SIZE) {
            throw new IllegalArgumentException("StatData byte array size mismatch: " + raw.length);
        }
        fromBytes(ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN));
    }

    /**
     * Native support extracted from StatData::Serialize @005157EE raw write.
     * Fully ported.
     */
    byte[] toBytes() {
        byte[] raw = new byte[BYTE_SIZE];
        toBytes(ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN));
        return raw;
    }

    /**
     * Native support extracted from StatData::Serialize @005157EE raw read.
     * Fully ported.
     */
    void fromBytes(ByteBuffer bb) {
        if (bb.remaining() < BYTE_SIZE) {
            throw new IllegalArgumentException("StatData byte buffer size mismatch: " + bb.remaining());
        }
        defence = bb.getShort();
        absorbtion = bb.getShort();
        for (int i = 0; i < 6; i++) {
            protections[i] = bb.getShort();
        }
        bb.get(m_bModifiers);
    }

    /**
     * Native support extracted from StatData::Serialize @005157EE raw write.
     * Fully ported.
     */
    void toBytes(ByteBuffer bb) {
        if (bb.remaining() < BYTE_SIZE) {
            throw new IllegalArgumentException("StatData byte buffer size mismatch: " + bb.remaining());
        }
        bb.putShort(defence);
        bb.putShort(absorbtion);
        for (int i = 0; i < 6; i++) {
            bb.putShort(protections[i]);
        }
        bb.put(m_bModifiers);
    }
}
