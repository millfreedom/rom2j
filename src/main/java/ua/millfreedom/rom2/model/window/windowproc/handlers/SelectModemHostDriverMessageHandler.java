package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SELECT_MODEM_HOST_DRIVER` branch in CMainWindow::WindowProc @004852D8.
 */
public final class SelectModemHostDriverMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private SelectModemHostDriverMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8 branch calling
     * CMainWindow::selectNetworkHostDriver @0048F1E2.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.selectNetworkHostDriver();
        return 1;
    }
}
