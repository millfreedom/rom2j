package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SHOW_CREDITS_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class CreditsDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private CreditsDialogMessageHandler() {
    }

    /**
     * Native support extracted from the `SHOW_CREDITS_DIALOG` branch in CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.showCreditsDialog();
        return 1;
    }
}
