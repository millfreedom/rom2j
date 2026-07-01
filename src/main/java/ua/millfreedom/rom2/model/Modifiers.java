package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.model.unit.Unit;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Native layout: Modifiers (size 0x40).
 * Native raw serialization: Modifiers::Serialize @005114bc.
 */
public class Modifiers implements MfcSerializable {
    public static final int BYTE_SIZE = 0x40;

    // field0_0x00 (native stat modifier: body)
    public byte body;
    // field1_0x01 (native stat modifier: reaction)
    public byte reaction;
    // field2_0x02 (native stat modifier: mind)
    public byte mind;
    // field3_0x03 (native stat modifier: spirit)
    public byte spirit;

    // field4_0x04 (native: Attack)
    public short attack;
    // field5_0x06 (native: MaxWeight)
    public short maxWeight;
    // field6_0x08 (native: MaxHP)
    public short maxHp;
    // field7_0x0A (inferred from Effect usage: HP regeneration)
    public short hpRegen;
    // field8_0x0C (native: MaxMP)
    public short maxMp;
    // field9_0x0E (inferred from Effect usage: MP regeneration)
    public short mpRegen;
    //0x10
    public short packedSightRange;

    // field11_0x12
    public final SkillData skillMods = new SkillData();

    // field12_0x2A
    public final StatData statMods = new StatData();

    /**
     * Native: Modifiers::New @00511266.
     * Fully ported.
     */
    public Modifiers() {
    }

    /**
     * Native support extracted from Modifiers::Modifiers @00511326 in-place field copy.
     * Fully ported.
     */
    public Modifiers assign(Modifiers from) {
        body = from.body;
        reaction = from.reaction;
        mind = from.mind;
        spirit = from.spirit;
        attack = from.attack;
        maxWeight = from.maxWeight;
        maxHp = from.maxHp;
        hpRegen = from.hpRegen;
        maxMp = from.maxMp;
        mpRegen = from.mpRegen;
        packedSightRange = from.packedSightRange;
        skillMods.assign(from.skillMods);
        statMods.assign(from.statMods);
        return this;
    }

    /**
     * Native: Modifiers::Add @005113E7.
     * Fully ported.
     */
    public void addToUnit(Unit unit) {
        unit.speed = (short) (unit.speed + attack);
        if (unit.speed < 0) {
            attack = 0;
        }
        unit.m_nMaxWeight = (short) (unit.m_nMaxWeight + maxWeight);
        unit.m_nMaxHP = (short) (unit.m_nMaxHP + maxHp);
        unit.m_nMaxMP = (short) (unit.m_nMaxMP + maxMp);

        int packedSight = (unit.sightFraction & 0xFF) | ((unit.sightRange & 0xFF) << 8);
        packedSight += packedSightRange;
        unit.sightFraction = packedSight & 0xFF;
        unit.sightRange = (packedSight >>> 8) & 0xFF;

        unit.unitStatData.add(statMods);
        unit.skillData.addPartially(skillMods);
    }

    /**
     * Native: Modifiers::Serialize @005114BC.
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
     * Native support extracted from Modifiers::Serialize @005114BC raw read.
     * Fully ported.
     */
    private void fromBytes(byte[] raw) {
        if (raw.length != BYTE_SIZE) {
            throw new IllegalArgumentException("Modifiers byte array size mismatch: " + raw.length);
        }
        fromBytes(ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN));
    }

    /**
     * Native support extracted from Modifiers::Serialize @005114BC raw read.
     * Fully ported.
     */
    private void fromBytes(ByteBuffer bb) {
        if (bb.remaining() < BYTE_SIZE) {
            throw new IllegalArgumentException("Modifiers byte buffer size mismatch: " + bb.remaining());
        }
        body = bb.get();
        reaction = bb.get();
        mind = bb.get();
        spirit = bb.get();
        attack = bb.getShort();
        maxWeight = bb.getShort();
        maxHp = bb.getShort();
        hpRegen = bb.getShort();
        maxMp = bb.getShort();
        mpRegen = bb.getShort();
        packedSightRange = bb.getShort();

        skillMods.fromBytes(bb);
        statMods.fromBytes(bb);
    }

    /**
     * Native support extracted from Modifiers::Serialize @005114BC raw write.
     * Fully ported.
     */
    private byte[] toBytes() {
        byte[] raw = new byte[BYTE_SIZE];
        toBytes(ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN));
        return raw;
    }

    /**
     * Native support extracted from Modifiers::Serialize @005114BC raw write.
     * Fully ported.
     */
    private void toBytes(ByteBuffer bb) {
        if (bb.remaining() < BYTE_SIZE) {
            throw new IllegalArgumentException("Modifiers byte buffer size mismatch: " + bb.remaining());
        }
        bb.put(body);
        bb.put(reaction);
        bb.put(mind);
        bb.put(spirit);
        bb.putShort(attack);
        bb.putShort(maxWeight);
        bb.putShort(maxHp);
        bb.putShort(hpRegen);
        bb.putShort(maxMp);
        bb.putShort(mpRegen);
        bb.putShort(packedSightRange);

        skillMods.toBytes(bb);
        statMods.toBytes(bb);
    }
}
