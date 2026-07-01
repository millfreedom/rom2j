package ua.millfreedom.rom2.model.unit.humanoid;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

import java.io.IOException;

/**
 * Skill bonus storage used by Humanoid serialization.
 * Native size: 0x18 (6 * int32).
 */
public class SkillBonus implements MfcSerializable {
    public final int[] data = new int[6];

    /**
     * Native support extracted from Humanoid::serialize @0052D7C8 0x18-byte skill bonus block.
     * Fully ported.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (!ar.isStoring()) {
            for (int i = 0; i < data.length; i++) {
                data[i] = ar.readInt();
            }
        } else {
            for (int value : data) {
                ar.writeInt(value);
            }
        }
    }
}
