package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native support extracted from the `CLIENT_RETURN_TO_MAP_SELECTION` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ClientReturnToMapSelectionMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ClientReturnToMapSelectionMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Partial port. Preserves the client lobby cleanup before rebuilding the multiplayer map/session list.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (mainWindow.sessionMode == CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT) {
            mainWindow.pMapVisualObject.clearSessionForAllPlayersLobbyReturn();
            if (GAMEPLAY.isSetIn(mainWindow.dialogsMask)) {
                mainWindow.cleanupGameplayResources();
            }
            mainWindow.postMessage(MessageCodes.PREPARE_MULTIPLAYER_MAP_SELECTION, 0, 0);
        }
        return 1;
    }
}
