package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SELECT_SERIAL_HOST_DRIVER` branch in CMainWindow::WindowProc @004852D8.
 */
public final class SelectSerialHostDriverMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private SelectSerialHostDriverMessageHandler() {
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
