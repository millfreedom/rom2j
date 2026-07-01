package ua.millfreedom.rom2.gameserver.missionruntime;

import ua.millfreedom.rom2.CArchive.CArchive;
import ua.millfreedom.rom2.CArchive.MfcSerializable;

import java.io.IOException;

/**
 * Native condition triple copied by ScriptCondition constructors @00573E48/@00573E83 and consumed by
 * MissionScriptRuntime::evaluateScriptPatterns @00574BA1.
 */
public final class ScriptCondition implements MfcSerializable {
    public static final int EQUAL = 0;
    public static final int NOT_EQUAL = 1;
    public static final int GREATER_THAN = 2;
    public static final int LESS_THAN = 3;
    public static final int GREATER_OR_EQUAL = 4;
    public static final int LESS_OR_EQUAL = 5;

    //0x00
    public int leftVariableIndex;
    //0x04
    public int rightVariableIndex;
    //0x08
    public int comparison;

    /**
     * Native: ScriptCondition::ScriptCondition @00573E48.
     * Fully ported.
     */
    public ScriptCondition() {
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadScriptPattern @00576944.
     */
    public ScriptCondition(int leftVariableIndex, int rightVariableIndex, int comparison) {
        this.leftVariableIndex = leftVariableIndex;
        this.rightVariableIndex = rightVariableIndex;
        this.comparison = comparison;
    }

    /**
     * Native: ScriptCondition::ScriptCondition @00573E83.
     * Fully ported.
     */
    public ScriptCondition(ScriptCondition from) {
        leftVariableIndex = from.leftVariableIndex;
        rightVariableIndex = from.rightVariableIndex;
        comparison = from.comparison;
    }

    /**
     * Native: ScriptCondition::Serialize @00579B8A.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        if (ar.isStoring()) {
            ar.writeInt(leftVariableIndex);
            ar.writeInt(rightVariableIndex);
            ar.writeInt(comparison);
        } else {
            leftVariableIndex = ar.readInt();
            rightVariableIndex = ar.readInt();
            comparison = ar.readInt();
        }
    }
}
