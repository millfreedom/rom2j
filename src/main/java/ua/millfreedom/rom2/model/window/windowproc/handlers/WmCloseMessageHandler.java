package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `WM_CLOSE` branch in CMainWindow::WindowProc @004852D8.
 */
public final class WmCloseMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private WmCloseMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.onClose();
        return 1;
    }
}
