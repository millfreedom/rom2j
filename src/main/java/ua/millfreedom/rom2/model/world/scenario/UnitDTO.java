package ua.millfreedom.rom2.model.world.scenario;

import java.nio.ByteBuffer;

/**
 * Native scenario unit record materialized by ScenarioDescriptor::ScenarioDescriptor @00534AD4.
 */
public final class UnitDTO {
    //0x00
    public int x;
    //0x04
    public int y;
    //0x08
    public int typeID;
    //0x0C
    public int face;
    //0x10
    public int serverID;
    //0x14
    public int playerID;
    //0x1C
    public int rotation;
    //0x20
    public int hp;
    //0x22
    public int maxHp;
    //0x24
    public int field16_0x24;
    //0x26
    public int field17_0x26;
    //0x28
    public int field18_0x28;
    //0x29
    public int field19_0x29;
    //0x2A
    public int field20_0x2a;
    //0x2B
    public int field21_0x2b;
    //0x2C
    public int field22_0x2c;
    //0x2D
    public int field23_0x2d;
    //0x2E
    public int field24_0x2e;
    //0x2F
    public int field25_0x2f;
    //0x30
    public int field26_0x30;
    //0x31
    public final byte[] field27_0x31 = new byte[6];
    //0x37
    public final byte[] field28_0x37 = new byte[5];
    //0x3C
    public int unitID;
    //0x40
    public int groupID;
    //0x44
    public int sackIDX = -1;
    //0x48
    public int unitFlags1;
    //0x4C
    public int field33_0x4c;
    //0x4E
    public int field34_0x4e;
    //0x50
    public int questFlags;

    /**
     * Native support extracted from ScenarioDescriptor::ScenarioDescriptor @00534AD4 units section.
     * Fully ported.
     */
    public static UnitDTO read(ByteBuffer section, int version) {
        UnitDTO unit = new UnitDTO();
        unit.x = section.getInt();
        unit.y = section.getInt();
        unit.typeID = Short.toUnsignedInt(section.getShort());
        unit.face = Short.toUnsignedInt(section.getShort());
        unit.unitFlags1 = section.getInt();
        if (version >= 0x47e) {
            unit.questFlags = section.getInt();
        } else {
            unit.questFlags = 0;
        }
        if (version >= 0x3db) {
            unit.serverID = section.getInt();
        } else {
            unit.serverID = 0;
        }
        unit.playerID = section.getInt() & 0xffff;
        unit.sackIDX = section.getInt() - 1;
        unit.rotation = section.getInt();
        unit.hp = Short.toUnsignedInt(section.getShort());
        unit.maxHp = Short.toUnsignedInt(section.getShort());
        if (version < 0x44c) {
            unit.field16_0x24 = Short.toUnsignedInt(section.getShort());
            unit.field17_0x26 = Short.toUnsignedInt(section.getShort());
            if (version > 0x3b6) {
                unit.field33_0x4c = Short.toUnsignedInt(section.getShort());
                unit.field34_0x4e = Short.toUnsignedInt(section.getShort());
            }
            unit.field18_0x28 = Byte.toUnsignedInt(section.get());
            unit.field19_0x29 = Byte.toUnsignedInt(section.get());
            unit.field20_0x2a = Byte.toUnsignedInt(section.get());
            unit.field21_0x2b = Byte.toUnsignedInt(section.get());
            unit.field22_0x2c = Byte.toUnsignedInt(section.get());
            unit.field23_0x2d = Byte.toUnsignedInt(section.get());
            if (version > 0x3d8) {
                unit.field24_0x2e = Byte.toUnsignedInt(section.get());
            }
            unit.field25_0x2f = Byte.toUnsignedInt(section.get());
            unit.field26_0x30 = Byte.toUnsignedInt(section.get());
            section.get(unit.field27_0x31);
            section.get(unit.field28_0x37);
        }
        if (version < 0x456) {
            unit.unitID = Short.toUnsignedInt(section.getShort());
        } else {
            unit.unitID = section.getInt();
        }
        unit.groupID = section.getInt();
        return unit;
    }
}
