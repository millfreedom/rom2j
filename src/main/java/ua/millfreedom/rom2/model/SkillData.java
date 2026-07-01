package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Native size: 0x18
 * +0x00 short    toHit
 * +0x02 short[6] skillLevels
 * +0x0E MinAndModifier2 skillDamageType0And3
 * +0x10 byte     activeSkillIndex
 * +0x11 MinAndModifier2 skillDamageType1
 * +0x13 MinAndModifier2 skillDamageType2
 * +0x15 byte     skillDamageType2ProtectionIndex
 * +0x16 byte     reserved0x16
 * +0x17 byte     reserved0x17
 */
public class SkillData implements MfcSerializable {
    public static final int BYTE_SIZE = 0x18;

    //0x00
    public short toHit;
    //0x02
    public final short[] skillLevels = new short[6];
    //0x0E
    public byte skillDamageType0And3Min;
    //0x0F
    public byte skillDamageType0And3Modifier;
    //0x10
    public byte activeSkillIndex;
    //0x11
    public byte skillDamageType1Min;
    //0x12
    public byte skillDamageType1Modifier;
    //0x13
    public byte skillDamageType2Min;
    //0x14
    public byte skillDamageType2Modifier;
    //0x15
    public byte skillDamageType2ProtectionIndex;
    //0x16
    public byte reserved0x16;
    //0x17
    public byte reserved0x17;

    /**
     * Native: SkillData::New @00515825 and SkillData::Init @0051583B.
     * Fully ported.
     */
    public SkillData() {
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 damage-profile payload.
     */
    public int damageMinimumSum() {
        return (skillDamageType0And3Min & 0xFF)
                + (skillDamageType1Min & 0xFF)
                + (skillDamageType2Min & 0xFF);
    }

    /**
     * Native support extracted from CServerApp::NetUpdate @00502019 damage-profile payload.
     */
    public int damageModifierSum() {
        return (skillDamageType0And3Modifier & 0xFF)
                + (skillDamageType1Modifier & 0xFF)
                + (skillDamageType2Modifier & 0xFF);
    }

    /**
     * Native: SkillData::AddPartially @00515A6C.
     * Fully ported.
     */
    public void addPartially(SkillData from) {
        toHit = (short) (toHit + from.toHit);
        skillDamageType0And3Min = (byte) (skillDamageType0And3Min + from.skillDamageType0And3Min);
        skillDamageType0And3Modifier = (byte) (skillDamageType0And3Modifier + from.skillDamageType0And3Modifier);
        skillDamageType1Min = (byte) (skillDamageType1Min + from.skillDamageType1Min);
        skillDamageType1Modifier = (byte) (skillDamageType1Modifier + from.skillDamageType1Modifier);
        skillDamageType2Min = (byte) (skillDamageType2Min + from.skillDamageType2Min);
        skillDamageType2Modifier = (byte) (skillDamageType2Modifier + from.skillDamageType2Modifier);
        skillDamageType2ProtectionIndex = from.skillDamageType2ProtectionIndex;
    }

    /**
     * Native: SkillData::Add @005158F5.
     * Fully ported.
     */
    public void add(SkillData other) {
        for (int i = 0; i < 6; i++) {
            skillLevels[i] = (short) (skillLevels[i] + other.skillLevels[i]);
        }
        toHit = (short) (toHit + other.toHit);
        skillDamageType0And3Min = (byte) (skillDamageType0And3Min + other.skillDamageType0And3Min);
        skillDamageType0And3Modifier = (byte) (skillDamageType0And3Modifier + other.skillDamageType0And3Modifier);
        skillDamageType1Min = (byte) (skillDamageType1Min + other.skillDamageType1Min);
        skillDamageType1Modifier = (byte) (skillDamageType1Modifier + other.skillDamageType1Modifier);
        skillDamageType2Min = (byte) (skillDamageType2Min + other.skillDamageType2Min);
        skillDamageType2Modifier = (byte) (skillDamageType2Modifier + other.skillDamageType2Modifier);
        skillDamageType2ProtectionIndex = other.skillDamageType2ProtectionIndex;
    }

    /**
     * Native: SkillData::Assign @005159CB.
     * Fully ported.
     */
    public void assign(SkillData from) {
        for (int i = 0; i < 6; i++) {
            skillLevels[i] = from.skillLevels[i];
        }
        toHit = from.toHit;
        skillDamageType0And3Min = from.skillDamageType0And3Min;
        skillDamageType0And3Modifier = from.skillDamageType0And3Modifier;
        skillDamageType1Min = from.skillDamageType1Min;
        skillDamageType1Modifier = from.skillDamageType1Modifier;
        skillDamageType2Min = from.skillDamageType2Min;
        skillDamageType2Modifier = from.skillDamageType2Modifier;
        skillDamageType2ProtectionIndex = from.skillDamageType2ProtectionIndex;
    }

    /**
     * Native: SkillData::Copy @005158D6.
     * Native support extracted from Effect_DirectDamage::copyFrom @0051E5DB.
     * Fully ported.
     */
    public SkillData copy(SkillData from) {
        toHit = from.toHit;
        for (int i = 0; i < 6; i++) {
            skillLevels[i] = from.skillLevels[i];
        }
        skillDamageType0And3Min = from.skillDamageType0And3Min;
        skillDamageType0And3Modifier = from.skillDamageType0And3Modifier;
        activeSkillIndex = from.activeSkillIndex;
        skillDamageType1Min = from.skillDamageType1Min;
        skillDamageType1Modifier = from.skillDamageType1Modifier;
        skillDamageType2Min = from.skillDamageType2Min;
        skillDamageType2Modifier = from.skillDamageType2Modifier;
        skillDamageType2ProtectionIndex = from.skillDamageType2ProtectionIndex;
        reserved0x16 = from.reserved0x16;
        reserved0x17 = from.reserved0x17;
        return this;
    }

    /**
     * Native: SkillData::Subtract @00515B03.
     * Fully ported.
     */
    public void subtract(SkillData v) {
        for (int i = 0; i < 6; i++) {
            skillLevels[i] = (short) (skillLevels[i] - v.skillLevels[i]);
        }
        toHit = (short) (toHit - v.toHit);
        skillDamageType0And3Min = (byte) (skillDamageType0And3Min - v.skillDamageType0And3Min);
        skillDamageType0And3Modifier = (byte) (skillDamageType0And3Modifier - v.skillDamageType0And3Modifier);
        skillDamageType1Min = (byte) (skillDamageType1Min - v.skillDamageType1Min);
        skillDamageType1Modifier = (byte) (skillDamageType1Modifier - v.skillDamageType1Modifier);
        skillDamageType2Min = (byte) (skillDamageType2Min - v.skillDamageType2Min);
        skillDamageType2Modifier = (byte) (skillDamageType2Modifier - v.skillDamageType2Modifier);
        if (((skillDamageType2Min & 0xFF) + (skillDamageType2Modifier & 0xFF)) != 0) {
            skillDamageType2ProtectionIndex = 0;
        }
    }

    /**
     * Native: SkillData::Serialize @00515BEA.
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
     * Native support extracted from SkillData::Serialize @00515BEA raw read.
     */
    void fromBytes(byte[] raw) {
        if (raw.length != BYTE_SIZE) {
            throw new IllegalArgumentException("SkillData byte array size mismatch: " + raw.length);
        }
        fromBytes(ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN));
    }

    /**
     * Native support extracted from SkillData::Serialize @00515BEA raw write.
     */
    byte[] toBytes() {
        byte[] raw = new byte[BYTE_SIZE];
        toBytes(ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN));
        return raw;
    }

    /**
     * Native support extracted from SkillData::Serialize @00515BEA raw read.
     */
    void fromBytes(ByteBuffer bb) {
        if (bb.remaining() < BYTE_SIZE) {
            throw new IllegalArgumentException("SkillData byte buffer size mismatch: " + bb.remaining());
        }
        toHit = bb.getShort();
        for (int i = 0; i < 6; i++) {
            skillLevels[i] = bb.getShort();
        }
        skillDamageType0And3Min = bb.get();
        skillDamageType0And3Modifier = bb.get();
        activeSkillIndex = bb.get();
        skillDamageType1Min = bb.get();
        skillDamageType1Modifier = bb.get();
        skillDamageType2Min = bb.get();
        skillDamageType2Modifier = bb.get();
        skillDamageType2ProtectionIndex = bb.get();
        reserved0x16 = bb.get();
        reserved0x17 = bb.get();
    }

    /**
     * Native support extracted from SkillData::Serialize @00515BEA raw write.
     */
    void toBytes(ByteBuffer bb) {
        if (bb.remaining() < BYTE_SIZE) {
            throw new IllegalArgumentException("SkillData byte buffer size mismatch: " + bb.remaining());
        }
        bb.putShort(toHit);
        for (int i = 0; i < 6; i++) {
            bb.putShort(skillLevels[i]);
        }
        bb.put(skillDamageType0And3Min);
        bb.put(skillDamageType0And3Modifier);
        bb.put(activeSkillIndex);
        bb.put(skillDamageType1Min);
        bb.put(skillDamageType1Modifier);
        bb.put(skillDamageType2Min);
        bb.put(skillDamageType2Modifier);
        bb.put(skillDamageType2ProtectionIndex);
        bb.put(reserved0x16);
        bb.put(reserved0x17);
    }
}
