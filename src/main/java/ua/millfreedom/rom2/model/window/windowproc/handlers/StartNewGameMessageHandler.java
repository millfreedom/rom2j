package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.ScriptDataSupport;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `START_NEW_GAME` branch in CMainWindow::WindowProc @004852D8.
 */
public final class StartNewGameMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private StartNewGameMessageHandler() {
    }

    /**
     * Native support extracted from the `START_NEW_GAME` branch in CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        ScriptDataSupport.loadTownScriptData();
        Globals.scenarioLib.newGame();
        mainWindow.serverLoopCounter = 0;
        mainWindow.serverBootstrapEnabled = 1;
        mainWindow.initializeNewCampaignSession();
        mainWindow.m_FameHall.zeroFactors();
        mainWindow.pGlobalMapDialogVisualObject.rebuildScenarioLocations();
        mainWindow.m_FameHall.setSelectedDifficulty(1);
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_DEDICATED_SERVER) {
            mainWindow.postMessage(MessageCodes.RUN_SESSION_BOOTSTRAP, 0, 0);
        } else {
            mainWindow.m_GameSession.refreshCharacterRosterFiles();
            mainWindow.m_GameSession.initializeNewCharacterSession(0, null);
            mainWindow.showStartGameSetupForNewSession();
        }
        return 1;
    }
}
