package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SHOW_STARTUP_LOGO_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowStartupLogoDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowStartupLogoDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.showStartupLogoDialog();
        mainWindow.pStartupLogoDialogVisualObject.onMessage(MessageCodes.STARTUP_LOGO_SET_STEP, 1, 0);
        mainWindow.pStartupLogoDialogVisualObject.onMessage(MessageCodes.STARTUP_LOGO_SET_TIMEOUT, 4000, 0);
        mainWindow.inputController.draw();
        return 1;
    }
}
