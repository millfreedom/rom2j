package ua.millfreedom.rom2.gameserver.missionruntime;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;
import ua.millfreedom.rom2.Utils;
import ua.millfreedom.rom2.model.container.CustomList;

import java.io.IOException;

/**
 * Native 0x18-byte trigger/pattern object stored in CList at MissionScriptRuntime +0xEAB4.
 */
public final class ScriptPattern implements MfcSerializable {
    //0x00
    public final CustomList<ScriptCondition> conditions = CustomList.std(ScriptCondition.class);
    //0x04
    public final CustomList<Integer> instantIds = CustomList.std(Integer.class);
    //0x08
    public int selfDestruct;
    // Serialized reserved int copied/archived by native ScriptPattern methods; no recovered active use.
    //0x0C
    public int reserved0x0C;
    // Serialized reserved int copied/archived by native ScriptPattern methods; no recovered active use.
    //0x10
    public int reserved0x10;
    //0x14
    public int digit;

    /**
     * Native: ScriptPattern::ScriptPattern @00573EB0.
     * Fully ported.
     */
    public ScriptPattern() {
    }

    /**
     * Native: ScriptPattern::ScriptPattern @005741A5.
     * Fully ported.
     */
    public ScriptPattern(ScriptPattern from) {
        copyFrom(from);
    }

    /**
     * Native: ScriptPattern::copyFrom @00573FDC.
     * Fully ported.
     */
    public void copyFrom(ScriptPattern from) {
        conditions.clear();
        instantIds.clear();
        selfDestruct = from.selfDestruct;
        reserved0x0C = from.reserved0x0C;
        reserved0x10 = from.reserved0x10;
        digit = from.digit;
        for (ScriptCondition condition : from.conditions) {
            conditions.add(new ScriptCondition(condition));
        }
        for (Integer instantId : from.instantIds) {
            instantIds.add(instantId);
        }
    }

    /**
     * Native: ScriptPattern::Serialize @00579BCC.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            ar.writeInt(Utils.encodePointerLike(conditions));
            ar.writeInt(Utils.encodePointerLike(instantIds));
            ar.writeInt(selfDestruct);
            ar.writeInt(reserved0x0C);
            ar.writeInt(reserved0x10);
            ar.writeInt(digit);
        } else {
            ar.readInt();
            ar.readInt();
            selfDestruct = ar.readInt();
            reserved0x0C = ar.readInt();
            reserved0x10 = ar.readInt();
            digit = ar.readInt();
        }
        ar.serialize(conditions);
        ar.serialize(instantIds);
    }

    /**
     * Native: ScriptPattern::restoreContext @00579D57.
     */
    public void restoreContext() {
    }
}
