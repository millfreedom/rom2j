package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CServerApp;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.world.ScenarioLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Native support extracted from the `CONTINUE_SCENARIO_LOCATION_ENTRY` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ContinueScenarioLocationEntryMessageHandler {

    private static final int SCENARIO_CHAPTER_VAR_ID = 0x300;
    private static final int SCENARIO_MISSION_ENTRY_COUNT = 0x14;
    private static final int SCENARIO_MISSION_ENTRY_AVAILABLE_VAR_BASE = 0x200;
    private static final int SCENARIO_MISSION_ENTRY_SELECTED_VAR_BASE = 0x214;
    private static final int SCENARIO_LOCATION_KIND_MISSION = 1;

    /**
     * Java utility constructor.
     * not ported.
     */
    private ContinueScenarioLocationEntryMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Partial port. Handles the recovered mission-vs-town continuation tree: mission locations gather and dispatch
     * active mission ids before session bootstrap; non-mission locations refresh shelves and return to town.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        ScenarioLocation currentLocation = Globals.scenarioLib.getCurrentLocation();
        if (currentLocation.kind == SCENARIO_LOCATION_KIND_MISSION) {
            List<Integer> missionIds1 = new ArrayList<>();
            for (int missionIndex = 0; missionIndex < SCENARIO_MISSION_ENTRY_COUNT; missionIndex++) {
                if (Globals.scenarioLib.getVar(SCENARIO_MISSION_ENTRY_AVAILABLE_VAR_BASE + missionIndex) != 0
                        && Globals.scenarioLib.getVar(SCENARIO_MISSION_ENTRY_SELECTED_VAR_BASE + missionIndex) != 0) {
                    missionIds1.add(missionIndex + 1);
                }
            }
            List<Integer> missionIds = missionIds1;
            if (!missionIds.isEmpty()) {
                int scenarioChapter = Globals.scenarioLib.getVar(SCENARIO_CHAPTER_VAR_ID);
                CServerApp.spawnScenarioMissionEntryUnits(scenarioChapter, missionIds);
                mainWindow.pMapVisualObject.pumpPendingGameActions();
            }
            mainWindow.runSessionBootstrap(0);
        } else {
            mainWindow.pMapVisualObject.refreshShopShelves();
            mainWindow.showCurrentTownDialog();
        }
        return 1;
    }
}
