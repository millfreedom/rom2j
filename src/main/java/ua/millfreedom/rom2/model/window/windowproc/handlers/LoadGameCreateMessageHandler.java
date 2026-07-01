package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;
import ua.millfreedom.rom2.model.world.ScenarioLocation;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native support extracted from the `LOAD_GAME_CREATE` branch in CMainWindow::WindowProc @004852D8.
 */
public final class LoadGameCreateMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private LoadGameCreateMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Partial port. Preserves the recovered active-session/server cleanup, location-id based `DIALOG_OK` replay after
     * load completion, and saved-campaign bootstrap handoff.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (DialogsMaskFlag.isExactly(mainWindow.dialogsMask, GAMEPLAY)) {
            mainWindow.cleanupActiveSessionForMenuReturn();
        }
        mainWindow.destroyServer();
        if (mainWindow.dialogsMask == 0 && mainWindow.inputController.getChildById(0x3FC) != null) {
            mainWindow.pMapVisualObject.clearSessionForFirstPlayerLobbyReturn();
            confirmCurrentTownDialogForLoadGame(mainWindow);
        }
        mainWindow.loadSelectedCampaignSaveGame();
        return 1;
    }

    /**
     * Native support extracted from the current-location `DIALOG_OK` replay in the `LOAD_GAME_CREATE` branch of
     * CMainWindow::WindowProc @004852D8.
     */
    private static void confirmCurrentTownDialogForLoadGame(CMainWindow mainWindow) {
        ScenarioLocation currentLocation = Globals.scenarioLib.getCurrentLocation();
        if (currentLocation != null) {
            if (currentLocation.id == 1) {
                mainWindow.pBasicTownDialogVisualObject.onMessage(MessageCodes.DIALOG_OK, 0, 0);
            } else if (currentLocation.id == 2) {
                mainWindow.pKaargTownDialogVisualObject.onMessage(MessageCodes.DIALOG_OK, 0, 0);
            } else if (currentLocation.id == 3) {
                mainWindow.pDruidTownDialogVisualObject.onMessage(MessageCodes.DIALOG_OK, 0, 0);
            }
        }
    }
}
