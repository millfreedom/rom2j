package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `PLAY_LOCATION_CUTSCENE_AND_CLOSE` branch in CMainWindow::WindowProc @004852D8.
 */
public final class PlayLocationCutsceneAndCloseMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private PlayLocationCutsceneAndCloseMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.playLocationCutscene(0);
        mainWindow.postMessage(MessageCodes.WM_CLOSE, 0, 0);
        return 1;
    }
}
