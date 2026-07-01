package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `EXIT_TO_MENU` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ExitToMenuMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ExitToMenuMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.cleanupActiveSessionForMenuReturn();
        mainWindow.postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
        return 1;
    }
}
