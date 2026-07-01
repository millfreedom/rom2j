package ua.millfreedom.rom2.gameserver.missionruntime;

/**
 * Native support structure embedded in MissionRuntimeBase at +0x08B8.
 */
public final class MissionTurnTimingStats {
    //0x04
    public int lastTurnElapsedMs;
    //0x08
    public int accumulatedTurnElapsedMs;
    //0x0C
    public int lastSegmentElapsedMs;
    //0x10
    public int accumulatedSegmentElapsedMs;
    //0x24
    public int lastActivatingElapsedMs;
    //0x28
    public int accumulatedActivatingElapsedMs;
    //0x2C
    public int lastScriptElapsedMs;
    //0x30
    public int accumulatedScriptElapsedMs;
    //0x34
    public int lastAiElapsedMs;
    //0x38
    public int accumulatedAiElapsedMs;

    /**
     * Native support extracted from MissionRuntimeBase::New @0056811B memset(this + 0x08B8, 0, 0x190).
     */
    public void clear() {
        lastTurnElapsedMs = 0;
        accumulatedTurnElapsedMs = 0;
        lastSegmentElapsedMs = 0;
        accumulatedSegmentElapsedMs = 0;
        lastActivatingElapsedMs = 0;
        accumulatedActivatingElapsedMs = 0;
        lastScriptElapsedMs = 0;
        accumulatedScriptElapsedMs = 0;
        lastAiElapsedMs = 0;
        accumulatedAiElapsedMs = 0;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processMissionTurn @0056FE4D turn timing bucket.
     */
    public void recordTurnElapsed(int elapsedMs) {
        lastTurnElapsedMs = elapsedMs;
        accumulatedTurnElapsedMs += elapsedMs;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processMissionTurn @0056FE4D activating timing bucket.
     */
    public void recordActivatingElapsed(int elapsedMs) {
        lastActivatingElapsedMs = elapsedMs;
        accumulatedActivatingElapsedMs += elapsedMs;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processMissionTurn @0056FE4D script timing bucket.
     */
    public void recordScriptElapsed(int elapsedMs) {
        lastScriptElapsedMs = elapsedMs;
        accumulatedScriptElapsedMs += elapsedMs;
    }

    /**
     * Native support extracted from MissionScriptRuntime::processMissionTurn @0056FE4D AI timing bucket.
     */
    public void recordAiElapsed(int elapsedMs) {
        lastAiElapsedMs = elapsedMs;
        accumulatedAiElapsedMs += elapsedMs;
    }
}
