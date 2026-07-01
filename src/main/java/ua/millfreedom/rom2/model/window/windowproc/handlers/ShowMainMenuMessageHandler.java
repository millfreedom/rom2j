package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SHOW_MAIN_MENU` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowMainMenuMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowMainMenuMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (mainWindow.dialogsMask == 0) {
            mainWindow.showMainMenu();
        }
        return 1;
    }
}
