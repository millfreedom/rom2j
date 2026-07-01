package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SHOW_FAME_HALL_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class FameHallDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private FameHallDialogMessageHandler() {
    }

    /**
     * Native support extracted from the `SHOW_FAME_HALL_DIALOG` branch in CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.showFameHallDialog();
        return 1;
    }
}
