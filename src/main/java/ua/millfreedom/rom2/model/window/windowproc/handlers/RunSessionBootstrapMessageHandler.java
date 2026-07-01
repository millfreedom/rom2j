package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `RUN_SESSION_BOOTSTRAP` branch in CMainWindow::WindowProc @004852D8.
 */
public final class RunSessionBootstrapMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private RunSessionBootstrapMessageHandler() {
    }

    /**
     * Native support extracted from the `RUN_SESSION_BOOTSTRAP` branch in CMainWindow::WindowProc @004852D8.
     * Calls CMainWindow::runSessionBootstrap @0048C8A3 with mode 0.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.runSessionBootstrap(0);
        return 1;
    }
}
