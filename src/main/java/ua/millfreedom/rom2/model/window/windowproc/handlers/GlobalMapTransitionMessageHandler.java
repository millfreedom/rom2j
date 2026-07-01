package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.world.ScenarioLocation;

/**
 * Native support extracted from the `SHOW_GLOBAL_MAP_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class GlobalMapTransitionMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private GlobalMapTransitionMessageHandler() {
    }

    /**
     * Native support extracted from the `SHOW_GLOBAL_MAP_DIALOG` branch in CMainWindow::WindowProc @004852D8.
     * Partial port. Preserves the recovered town-location-gated ScenarioLeaveLocation handoff, global-map origin
     * priming, town-1 special variant flag, cutscene playback, and global-map dialog transition.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        ScenarioLocation currentLocation = Globals.scenarioLib.getCurrentLocation();
        mainWindow.pGlobalMapDialogVisualObject.umoirMapMode = 0;
        if (currentLocation.kind == 2) {
            ScenarioLocation travelOriginLocation = Globals.scenarioLib.getCurrentLocation();
            mainWindow.pGlobalMapDialogVisualObject.setTravelOrigin(
                    travelOriginLocation.rect.left,
                    travelOriginLocation.rect.top
            );
            int[] cutsceneId = {-1};
            Globals.scenarioLib.leaveLocation(cutsceneId);
            if (cutsceneId[0] >= 0) {
                mainWindow.playLocationCutscene(cutsceneId[0]);
            }
            if (currentLocation.id == 1) {
                mainWindow.pGlobalMapDialogVisualObject.umoirMapMode = 1;
            }
        }
        mainWindow.showGlobalMapDialog();
        return 1;
    }
}
