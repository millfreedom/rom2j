package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `EXIT_GAME` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ExitGameMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ExitGameMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.postMessage(MessageCodes.WM_CLOSE, 0, 0);
        return 1;
    }
}
