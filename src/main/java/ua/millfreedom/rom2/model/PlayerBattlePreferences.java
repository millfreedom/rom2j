package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

import java.io.IOException;

/**
 * Native 0x24-byte player battle-preferences payload serialized at Player +0x34.
 */
public class PlayerBattlePreferences implements MfcSerializable {
    private static final int RESERVED_0X00_SIZE = 0x1F;
    private static final int RESERVED_0X21_SIZE = 0x03;

    //0x00
    public byte[] reserved0x00 = new byte[RESERVED_0X00_SIZE];
    //0x1f
    public int formationMode;
    //0x20
    public int autoCasting;
    //0x21
    public byte[] reserved0x21 = new byte[RESERVED_0X21_SIZE];

    /**
     * Native: PlayerBattlePreferences::New @005691C7.
     * Fully ported.
     */
    public PlayerBattlePreferences() {
        reserved0x00[8] = 0;
        reserved0x00[9] = 0;
        formationMode = 2;
        autoCasting = 0xFF;
    }

    /**
     * Native: PlayerBattlePreferences::Serialize @00574656.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            ar.writeBytes(reserved0x00);
            ar.writeByte(formationMode);
            ar.writeByte(autoCasting);
            ar.writeBytes(reserved0x21);
        } else {
            reserved0x00 = ar.readBytes(RESERVED_0X00_SIZE);
            formationMode = ar.readByte() & 0xFF;
            autoCasting = ar.readByte() & 0xFF;
            reserved0x21 = ar.readBytes(RESERVED_0X21_SIZE);
        }
    }
}
