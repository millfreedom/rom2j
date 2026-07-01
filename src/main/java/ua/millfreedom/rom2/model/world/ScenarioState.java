package ua.millfreedom.rom2.model.world;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Structured view of Scenario.dll `g_scenarioReadBuffer` @10036028.
 * Field comments include both the original dword index and the original byte offset inside the 4096-byte buffer.
 */
public final class ScenarioState {
    public static final int DWORD_COUNT = 0x1000 / 4;
    public static final int LOCATION_ENTRY_COUNT = 0x14;
    public static final int CURRENT_LOCATION_TRANSIENT_COUNT = 0x10;
    public static final int COMPLETED_LOCATION_FLAG_COUNT = 0x80;

    /**
     * `g_scenarioReadBuffer[0x000..0x1ff]` / byte offsets `0x0000..0x07ff`, not ported.
     */
    public final int[] reserved000To1ff = new int[0x200];

    /**
     * `g_scenarioReadBuffer[0x200..0x213]` / byte offsets `0x0800..0x084f` used by `ScenarioTalkTo` and `ScenarioEnterInn`.
     */
    public final int[] locationEntrySelectionFlags = new int[LOCATION_ENTRY_COUNT];

    /**
     * `g_scenarioReadBuffer[0x214..0x227]` / byte offsets `0x0850..0x089f` used by `ScenarioLeaveLocation` and `ScenarioEnterInn`.
     */
    public final int[] locationEntryStates = new int[LOCATION_ENTRY_COUNT];

    /**
     * `g_scenarioReadBuffer[0x228..0x23b]` / byte offsets `0x08a0..0x08ef` used by `ScenarioTalkTo`, `ScenarioLeaveLocation`, and `ScenarioEnterInn`.
     */
    public final int[] locationEntryLocationIds = new int[LOCATION_ENTRY_COUNT];

    /**
     * `g_scenarioReadBuffer[0x23c..0x2ef]` / byte offsets `0x08f0..0x0bbf`, not ported.
     */
    public final int[] reserved23cTo2ef = new int[0xb4];

    /**
     * `g_scenarioReadBuffer[0x2f0..0x2ff]` / byte offsets `0x0bc0..0x0bff` cleared by `ScenarioEnterLocation` @10001d22.
     */
    public final int[] currentLocationTransientFlags = new int[CURRENT_LOCATION_TRANSIENT_COUNT];

    /**
     * `g_scenarioReadBuffer[0x300]` / byte offset `0x0c00` used as the scenario chapter/phase in `ScenarioEnterInn` @1000286b.
     */
    public int scenarioChapter;

    /**
     * `g_scenarioReadBuffer[0x301]` / byte offset `0x0c04` observed by Java callers as start-mission availability.
     */
    public int startMissionAllowed;

    /**
     * `g_scenarioReadBuffer[0x302]` / byte offset `0x0c08`, special dialog state.
     */
    public int specialDialogState;

    /**
     * `g_scenarioReadBuffer[0x303]` / byte offset `0x0c0c`, inn-scene unlock flag.
     */
    public int innSceneUnlocked;

    /**
     * `g_scenarioReadBuffer[0x304]` / byte offset `0x0c10`, follow-up location unlock flag.
     */
    public int followupLocationUnlockFlag;

    /**
     * `g_scenarioReadBuffer[0x305]` / byte offset `0x0c14`, transition state.
     */
    public int locationTransitionState;

    /**
     * `g_scenarioReadBuffer[0x306]` / byte offset `0x0c18`, later-scene state.
     */
    public int laterSceneState;

    /**
     * `g_scenarioReadBuffer[0x307]` / byte offset `0x0c1c` rebuild gate in `FUN_10001da5` @10001da5.
     */
    public int rebuildAvailableLocationsPending;

    /**
     * `g_scenarioReadBuffer[0x308]` / byte offset `0x0c20`, mage branch selector.
     */
    public int isMage;

    /**
     * `g_scenarioReadBuffer[0x309]` / byte offset `0x0c24`, paired objective completion A.
     */
    public int pairedObjectiveACompleted;

    /**
     * `g_scenarioReadBuffer[0x30a]` / byte offset `0x0c28`, paired objective completion B.
     */
    public int pairedObjectiveBCompleted;

    /**
     * `g_scenarioReadBuffer[0x30b]` / byte offset `0x0c2c`, ending variant flag.
     */
    public int endingVariantFlag;

    /**
     * `g_scenarioReadBuffer[0x30c]` / byte offset `0x0c30` gating late addition of mission 52 in `OnCompleted` @10001DA5.
     */
    public int mission52Unlocked;

    /**
     * `g_scenarioReadBuffer[0x30d]` / byte offset `0x0c34`, female branch selector.
     */
    public int isFemale;

    /**
     * `g_scenarioReadBuffer[0x30e..0x37f]` / byte offsets `0x0c38..0x0dff`, not ported.
     */
    public final int[] reserved30eTo37f = new int[0x72];

    /**
     * `g_scenarioReadBuffer[0x380..0x3ff]` / byte offsets `0x0e00..0x0fff` location completion flags indexed by location id.
     */
    public final int[] completedLocationFlags = new int[COMPLETED_LOCATION_FLAG_COUNT];

    /**
     * Support method for the structured Scenario.dll buffer model, not ported.
     */
    public void clear() {
        Arrays.fill(reserved000To1ff, 0);
        Arrays.fill(locationEntrySelectionFlags, 0);
        Arrays.fill(locationEntryStates, 0);
        Arrays.fill(locationEntryLocationIds, 0);
        Arrays.fill(reserved23cTo2ef, 0);
        Arrays.fill(currentLocationTransientFlags, 0);
        scenarioChapter = 0;
        startMissionAllowed = 0;
        specialDialogState = 0;
        innSceneUnlocked = 0;
        followupLocationUnlockFlag = 0;
        locationTransitionState = 0;
        laterSceneState = 0;
        rebuildAvailableLocationsPending = 0;
        isMage = 0;
        pairedObjectiveACompleted = 0;
        pairedObjectiveBCompleted = 0;
        endingVariantFlag = 0;
        mission52Unlocked = 0;
        isFemale = 0;
        Arrays.fill(reserved30eTo37f, 0);
        Arrays.fill(completedLocationFlags, 0);
    }

    /**
     * Support method for the structured Scenario.dll buffer model, not ported.
     */
    public int getDword(int dwordIndex) {
        if (dwordIndex < 0 || dwordIndex >= DWORD_COUNT) {
            return 0;
        }
        if (dwordIndex < 0x200) {
            return reserved000To1ff[dwordIndex];
        }
        if (dwordIndex < 0x214) {
            return locationEntrySelectionFlags[dwordIndex - 0x200];
        }
        if (dwordIndex < 0x228) {
            return locationEntryStates[dwordIndex - 0x214];
        }
        if (dwordIndex < 0x23c) {
            return locationEntryLocationIds[dwordIndex - 0x228];
        }
        if (dwordIndex < 0x2f0) {
            return reserved23cTo2ef[dwordIndex - 0x23c];
        }
        if (dwordIndex < 0x300) {
            return currentLocationTransientFlags[dwordIndex - 0x2f0];
        }
        return switch (dwordIndex) {
            case 0x300 -> scenarioChapter;
            case 0x301 -> startMissionAllowed;
            case 0x302 -> specialDialogState;
            case 0x303 -> innSceneUnlocked;
            case 0x304 -> followupLocationUnlockFlag;
            case 0x305 -> locationTransitionState;
            case 0x306 -> laterSceneState;
            case 0x307 -> rebuildAvailableLocationsPending;
            case 0x308 -> isMage;
            case 0x309 -> pairedObjectiveACompleted;
            case 0x30a -> pairedObjectiveBCompleted;
            case 0x30b -> endingVariantFlag;
            case 0x30c -> mission52Unlocked;
            case 0x30d -> isFemale;
            default -> dwordIndex < 0x380
                    ? reserved30eTo37f[dwordIndex - 0x30e]
                    : completedLocationFlags[dwordIndex - 0x380];
        };
    }

    /**
     * Support method for the structured Scenario.dll buffer model, not ported.
     */
    public void setDword(int dwordIndex, int value) {
        if (dwordIndex < 0 || dwordIndex >= DWORD_COUNT) {
            return;
        }
        if (dwordIndex < 0x200) {
            reserved000To1ff[dwordIndex] = value;
            return;
        }
        if (dwordIndex < 0x214) {
            locationEntrySelectionFlags[dwordIndex - 0x200] = value;
            return;
        }
        if (dwordIndex < 0x228) {
            locationEntryStates[dwordIndex - 0x214] = value;
            return;
        }
        if (dwordIndex < 0x23c) {
            locationEntryLocationIds[dwordIndex - 0x228] = value;
            return;
        }
        if (dwordIndex < 0x2f0) {
            reserved23cTo2ef[dwordIndex - 0x23c] = value;
            return;
        }
        if (dwordIndex < 0x300) {
            currentLocationTransientFlags[dwordIndex - 0x2f0] = value;
            return;
        }
        switch (dwordIndex) {
            case 0x300 -> scenarioChapter = value;
            case 0x301 -> startMissionAllowed = value;
            case 0x302 -> specialDialogState = value;
            case 0x303 -> innSceneUnlocked = value;
            case 0x304 -> followupLocationUnlockFlag = value;
            case 0x305 -> locationTransitionState = value;
            case 0x306 -> laterSceneState = value;
            case 0x307 -> rebuildAvailableLocationsPending = value;
            case 0x308 -> isMage = value;
            case 0x309 -> pairedObjectiveACompleted = value;
            case 0x30a -> pairedObjectiveBCompleted = value;
            case 0x30b -> endingVariantFlag = value;
            case 0x30c -> mission52Unlocked = value;
            case 0x30d -> isFemale = value;
            default -> {
                if (dwordIndex < 0x380) {
                    reserved30eTo37f[dwordIndex - 0x30e] = value;
                } else {
                    completedLocationFlags[dwordIndex - 0x380] = value;
                }
            }
        }
    }

    /**
     * Support method for the structured Scenario.dll buffer model, not ported.
     */
    public void clearDwordRange(int startInclusive, int count) {
        for (int dwordIndex = 0; dwordIndex < count; dwordIndex++) {
            setDword(startInclusive + dwordIndex, 0);
        }
    }

    /**
     * Support method for the structured Scenario.dll buffer model, not ported.
     */
    public void writeTo(ByteBuffer buffer) {
        for (int dwordIndex = 0; dwordIndex < DWORD_COUNT; dwordIndex++) {
            buffer.putInt(getDword(dwordIndex));
        }
    }

    /**
     * Support method for the structured Scenario.dll buffer model, not ported.
     */
    public void readFrom(ByteBuffer buffer) {
        for (int dwordIndex = 0; dwordIndex < DWORD_COUNT; dwordIndex++) {
            setDword(dwordIndex, buffer.getInt());
        }
    }
}
