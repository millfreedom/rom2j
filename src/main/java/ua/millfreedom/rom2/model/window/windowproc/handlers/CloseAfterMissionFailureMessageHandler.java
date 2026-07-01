package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `CLOSE_AFTER_MISSION_FAILURE` branch in CMainWindow::WindowProc @004852D8.
 */
public final class CloseAfterMissionFailureMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private CloseAfterMissionFailureMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.postMessage(MessageCodes.PLAY_LOCATION_CUTSCENE_AND_CLOSE, 0, 0);
        return 1;
    }
}
