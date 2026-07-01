package ua.millfreedom.rom2.model;

import ua.millfreedom.rom2.CTextFile;

import java.util.ArrayList;
import java.util.List;

import static ua.millfreedom.rom2.CTextFile.loadTextFileToOem;
import static ua.millfreedom.rom2.text.GameTexts.get;
import static ua.millfreedom.rom2.text.StringTableIndex.*;

/**
 * Native support for parsing the current g_strScriptData @006227C4 buffer.
 */
public final class ScriptDataSupport {
    private static final String MISSION_TEXT_TEMPLATE = "main/text/mission%d.txt";
    private static final String QUEST_TEXT_PATH = "main/text/quest.txt";
    private static final String TOWN_TEXT_PATH = "main/text/town.txt";
    private static final String GLOBAL_MAP_TEXT_PATH = "main/text/globalmap.txt";
    private static final int MARKER_TEXT_SEPARATOR_LENGTH = 2;
    // Native global g_strScriptData @006227C4.
    // Static CString lifecycle thunks @00473F1F/@00473F2E/@00473F3D/@00473F4F are represented by Java object lifecycle.
    // Native global g_globalMapTextFile @00622720 is represented by this script-data line store.
    // Static CTextFile lifecycle thunks @00473CA7/@00473CB6/@00473CC5/@00473CD7 are represented by Java object lifecycle.
    public static final List<String> scriptData = new ArrayList<>();
    // Native global g_briefing_text @00622680.
    // Static CString lifecycle thunks @00473EA1/@00473EB0/@00473EBF/@00473ED1 are represented by Java object lifecycle.
    public static String briefingText = "";
    // Native global g_failureReasons @006227B0.
    // Static CStringArray lifecycle thunks @00473F5E/@00473F6D/@00473F7C/@00473F8E are represented by Java object lifecycle.
    public static final List<String> failureReasons = new ArrayList<>();
    // Native global g_Subobjectives @00622740.
    // Static CStringArray lifecycle thunks @00473F9D/@00473FAC/@00473FBB/@00473FCD are represented by Java object lifecycle.
    public static final List<String> subobjectives = new ArrayList<>();

    /**
     * Java utility constructor.
     * not ported.
     */
    private ScriptDataSupport() {
    }

    /**
     * Native support extracted from LoadTextFileToOEM @004741AD call sites targeting g_strScriptData @006227C4.
     */
    public static void loadScriptDataFromFile(String fileName) {
        scriptData.clear();
        scriptData.addAll(loadTextFileToOem(fileName).lines);
    }

    /**
     * Native support extracted from CTextFile::Delete(&g_globalMapTextFile) in CMainApp::ExitInstance @00481B92.
     */
    public static void clearScriptDataTextFile() {
        scriptData.clear();
    }

    /**
     * Native support extracted from LoadTextFileToOEM("main/text/mission%d.txt", &g_strScriptData) and the
     * g_briefing_text/g_failureReasons/g_Subobjectives refresh in CMainWindow::runSessionBootstrap @0048C8A3.
     */
    public static void loadCampaignMissionScriptData(int missionId) {
        loadScriptDataFromFile(MISSION_TEXT_TEMPLATE.formatted(missionId));
        briefingText = getBriefingText();
        failureReasons.clear();
        failureReasons.addAll(loadFailureReasons());
        subobjectives.clear();
        subobjectives.addAll(loadSubobjectives());
    }

    /**
     * Native support extracted from LoadTextFileToOEM("main/text/quest.txt", &g_strScriptData) in
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    public static void loadQuestScriptData() {
        loadScriptDataFromFile(QUEST_TEXT_PATH);
    }

    /**
     * Native support extracted from LoadTextFileToOEM("main/text/town.txt", &g_strScriptData) calls in
     * CMainWindow::WindowProc @004852D8 and CMainWindow::ShowCurrentTownDialog @0048BBC0.
     */
    public static void loadTownScriptData() {
        loadScriptDataFromFile(TOWN_TEXT_PATH);
    }

    /**
     * Native support extracted from LoadTextFileToOEM("main/text/globalmap.txt", &g_strScriptData) in
     * GlobalMapDialogVisualObject::ShowDialog @0046FA4B.
     */
    public static void loadGlobalMapScriptData() {
        loadScriptDataFromFile(GLOBAL_MAP_TEXT_PATH);
    }

    /**
     * Native: GetBriefingText @004DDABE.
     * Fully ported.
     */
    public static String getBriefingText() {
        return extractMarkedBlock("#briefing");
    }

    /**
     * Native support extracted from GetFailureMessage @004DDB87 call loop in
     * CMainWindow::runSessionBootstrap @0048C8A3.
     */
    public static List<String> loadFailureReasons() {
        List<String> failureReasons = new ArrayList<>();
        for (int failureId = 2; ; failureId++) {
            String failureMessage = getFailureMessage(failureId);
            if (failureMessage.isEmpty()) {
                if (failureId > 4) {
                    return failureReasons;
                }
                failureMessage = getDefaultFailureMessage(failureId);
            }
            failureReasons.add(failureMessage);
        }
    }

    /**
     * Native support extracted from GetSubobjective @004DDDB1 call loop in CMainWindow::runSessionBootstrap @0048C8A3.
     */
    public static List<String> loadSubobjectives() {
        List<String> subobjectives = new ArrayList<>();
        for (int objectiveId = 0; ; objectiveId++) {
            String subobjective = getSubobjective(objectiveId);
            if (subobjective.isEmpty()) {
                return subobjectives;
            }
            subobjectives.add(subobjective);
        }
    }

    /**
     * Native: ResolveTownTipText @004DDC9C.
     * Fully ported.
     */
    public static String getTipText(int tipIndex) {
        return extractMarkedBlock("#tips" + tipIndex);
    }

    /**
     * Native: GetGlobalMapLocationTitleText @004DDEC6.
     * Fully ported.
     */
    public static String getGlobalMapLocationTitleText(int locationKind, int locationId) {
        return extractMarkedBlock((locationKind == 1 ? "#title" : "#town") + locationId);
    }

    /**
     * Native: GetGlobalMapLocationDescriptionText @004DDFF8.
     * Fully ported.
     */
    public static String getGlobalMapLocationDescriptionText(int locationKind, int locationId) {
        return extractMarkedBlock((locationKind == 1 ? "#briefing" : "#description") + locationId);
    }

    /**
     * Native: GetQuestText @004DE12A.
     * Fully ported.
     */
    public static String getQuestText(int questId) {
        return extractMarkedBlock("#quest" + questId);
    }

    /**
     * Native support for CString::Find(&g_strScriptData, ...) call sites such as
     * RoleKeyDialogVisualObject::InitializeRoleDialog @004DD05B.
     */
    public static String getJoinedScriptText() {
        if (scriptData.isEmpty()) {
            return "";
        }
        return String.join("\r\n", scriptData);
    }

    /**
     * Native: GetFailureMessage @004DDB87.
     * Fully ported.
     */
    private static String getFailureMessage(int failureId) {
        return extractMarkedBlock("#failure" + failureId);
    }

    /**
     * Native support extracted from fallback g_StringTable reads in CMainWindow::runSessionBootstrap @0048C8A3.
     */
    private static String getDefaultFailureMessage(int failureId) {
        return switch (failureId) {
            case 2 -> get(MAIN_OH_NO_THE_MAIN_HERO_IS_DEAD_282);
            case 3 -> get(MAIN_SECOND_HERO_WAS_BITEN_AND_EATEN_BY_MONSTEROUS_BEASTS_283);
            case 4 -> get(MAIN_THIRD_HERO_DIES_IN_THE_WORST_POSSIBLE_WAY_284);
            default -> "";
        };
    }

    /**
     * Native: GetSubobjective @004DDDB1.
     * Fully ported.
     */
    private static String getSubobjective(int objectiveId) {
        return extractMarkedBlock("#subobjective" + objectiveId);
    }

    /**
     * Native support extracted from GetBriefingText @004DDABE, GetFailureMessage @004DDB87, and
     * ResolveTownTipText @004DDC9C, GetSubobjective @004DDDB1, GetGlobalMapLocationTitleText @004DDEC6,
     * GetGlobalMapLocationDescriptionText @004DDFF8, and GetQuestText @004DE12A.
     */
    private static String extractMarkedBlock(String marker) {
        String scriptText = getJoinedScriptText();
        int markerOffset = scriptText.indexOf(marker);
        if (markerOffset < 0) {
            return "";
        }

        int blockStart = Math.min(scriptText.length(), markerOffset + marker.length() + MARKER_TEXT_SEPARATOR_LENGTH);
        int blockEnd = scriptText.indexOf('#', blockStart);
        if (blockEnd < 0) {
            blockEnd = scriptText.length();
        }
        return scriptText.substring(blockStart, blockEnd);
    }
}
