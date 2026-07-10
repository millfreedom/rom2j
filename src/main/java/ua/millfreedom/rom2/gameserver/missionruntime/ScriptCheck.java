package ua.millfreedom.rom2.gameserver.missionruntime;

import ua.millfreedom.rom2.CArchive.CArchive;

import java.io.IOException;

/**
 * Native ScriptCheck object stored in CList<ScriptCheck> at MissionScriptRuntime +0xEAAC.
 */
public final class ScriptCheck extends ScriptInstant {
    //0x74 Native Execute Once BOOL; archived but not behaviorally read after materialization.
    public boolean executeOnce;
    //0x78
    public int referenceCount;

    /**
     * Native: ScriptCheck::ScriptCheck @00573D54.
     * Fully ported.
     */
    public ScriptCheck() {
        executeOnce = true;
    }

    /**
     * Native: ScriptCheck::ScriptCheck @00573D87 and @00573DA6.
     * Fully ported.
     */
    public ScriptCheck(ScriptCheck from) {
        super(from);
        executeOnce = from.executeOnce;
        referenceCount = from.referenceCount;
    }

    /**
     * Native support extracted from MissionScriptRuntime::loadScriptCheck @00575D05.
     */
    public ScriptCheck(int digit, int type) {
        this();
        this.digit = digit;
        this.type = type;
    }

    /**
     * Native: ScriptCheck::Serialize @005799D6.
     */
    @Override
    public void serialize(CArchive ar) throws IOException {
        super.serialize(ar);
        if (ar.isStoring()) {
            ar.writeInt(executeOnce ? 1 : 0);
            ar.writeInt(referenceCount);
        } else {
            executeOnce = ar.readInt() != 0;
            referenceCount = ar.readInt();
        }
    }

    /**
     * Native: ScriptCheck::restoreContext @00579A0D.
     */
    @Override
    public void restoreContext() {
        super.restoreContext();
    }
}
