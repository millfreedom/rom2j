package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.Globals;

public final class SkillProgression {
    private static final int NATIVE_COMPOUNDING_SKILL_LIMIT = 0x96;
    // Native global compounding10PctPermille @006A8998.
    private static final int[] COMPOUNDING_10PCT_PERMILLE = makeTableOfCompounding10PctPermille();

    /**
     * Java utility constructor.
     * not ported.
     */
    private SkillProgression() {
    }

    /**
     * Native support extracted from the makeTableOfCompounding10PctPermille @00512824 call in CMainApp::InitInstance @00481AB6.
     */
    public static void initializeNativeCompoundingTable() {
    }

    /**
     * Native: get10pctCompoundPermille @005128AC.
     * Fully ported.
     */
    public static int get10pctCompoundPermille(int skillLevel) {
        int cappedSkillLevel = Math.min(skillLevel, 0x95);
        return COMPOUNDING_10PCT_PERMILLE[cappedSkillLevel];
    }

    /**
     * Native: makeTableOfCompounding10PctPermille @00512824.
     * Fully ported.
     */
    private static int[] makeTableOfCompounding10PctPermille() {
        int[] table = new int[NATIVE_COMPOUNDING_SKILL_LIMIT];
        for (int skillLevel = 0; skillLevel < NATIVE_COMPOUNDING_SKILL_LIMIT; skillLevel++) {
            table[skillLevel] = (int) ((Math.pow(1.1d, skillLevel) - 1.0d) * 1000.0d);
        }
        return table;
    }

    /**
     * Native: skillLevelForBonusPermille @00512870.
     * Fully ported.
     */
    public static int skillLevelForBonusPermille(int bonusPermille) {
        int skillLevel = 0;
        do {
            if (bonusPermille < get10pctCompoundPermille(skillLevel + 1)) {
                return skillLevel;
            }
            skillLevel++;
        } while (skillLevel < NATIVE_COMPOUNDING_SKILL_LIMIT);
        return COMPOUNDING_10PCT_PERMILLE[NATIVE_COMPOUNDING_SKILL_LIMIT - 1];
    }

    /**
     * Native: logBaseOnePointOne @005127F2.
     * Fully ported.
     */
    public static double logBaseOnePointOne(double value) {
        return Math.log(value) / Math.log(1.1d);
    }

    /**
     * Native support extracted from Humanoid::recalculateDerivedStats @0051366B using
     * logBaseOnePointOne @005127F2.
     */
    public static double convertSkillBonusPermilleToCompoundLevel(int skillBonusPermille) {
        return logBaseOnePointOne(skillBonusPermille / 5000.0d + 1.0d);
    }

    /**
     * Native: clampAwardedSkillProgressPermille @00512F44.
     * Fully ported.
     */
    public static int clampAwardedSkillProgressPermille(int awardedProgressPermille, int currentSkillLevel) {
        int thresholdDeltaPermille = get10pctCompoundPermille(currentSkillLevel + 1)
                - get10pctCompoundPermille(currentSkillLevel);
        if (Globals.gameServer.networkSessionActive != 0) {
            thresholdDeltaPermille /= 5;
        }
        return Math.min(awardedProgressPermille, thresholdDeltaPermille);
    }
}
