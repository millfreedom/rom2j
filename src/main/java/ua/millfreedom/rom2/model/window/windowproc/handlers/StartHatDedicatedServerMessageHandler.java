package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `START_HAT_DEDICATED_SERVER` branch in CMainWindow::WindowProc @004852D8.
 */
public final class StartHatDedicatedServerMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private StartHatDedicatedServerMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.startHatDedicatedServer();
        return 1;
    }
}
