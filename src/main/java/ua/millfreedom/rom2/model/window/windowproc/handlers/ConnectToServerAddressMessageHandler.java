package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `CONNECT_TO_SERVER_ADDRESS` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ConnectToServerAddressMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ConnectToServerAddressMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.connectToServerAddress();
        return 1;
    }
}
