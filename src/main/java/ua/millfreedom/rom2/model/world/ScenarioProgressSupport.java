package ua.millfreedom.rom2.model.world;

import ua.millfreedom.rom2.Globals;

/**
 * Native global scenario-progress helpers used by CMainWindow close and map-exit routing.
 */
public final class ScenarioProgressSupport {
    // Scenario var id read by isCampaignEndingReached @00493DA0.
    private static final int SCENARIO_CHAPTER_VAR_ID = 0x300;
    // Scenario chapter threshold checked by isCampaignEndingReached @00493DA0.
    private static final int FAME_HALL_AFTER_CREDITS_THRESHOLD = 0x77;
    // Scenario var id read by GetPostMissionInnEntryId @00493DE0.
    private static final int POST_MISSION_INN_ENTRY_VAR_ID = 0x305;

    /**
     * Java utility constructor.
     * not ported.
     */
    private ScenarioProgressSupport() {
    }

    /**
     * Native: isCampaignEndingReached @00493DA0.
     * Fully ported.
     */
    public static boolean isCampaignEndingReached() {
        return Globals.scenarioLib.getVar(SCENARIO_CHAPTER_VAR_ID) > FAME_HALL_AFTER_CREDITS_THRESHOLD;
    }

    /**
     * Native: scenarioCurrentLocationID @00493DC0.
     * Fully ported.
     */
    public static int scenarioCurrentLocationId() {
        return Globals.scenarioLib.getCurrentLocation().id;
    }

    /**
     * Native: GetPostMissionInnEntryId @00493DE0.
     * Fully ported.
     */
    public static int getPostMissionInnEntryId() {
        return Globals.scenarioLib.getVar(POST_MISSION_INN_ENTRY_VAR_ID);
    }
}
