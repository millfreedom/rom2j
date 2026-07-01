package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `WM_USER` branch in CMainWindow::WindowProc @004852D8.
 */
public final class UserMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private UserMessageHandler() {
    }

    /**
     * Native support extracted from the `WM_USER` branch in CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.inputController.onMessage(MessageCodes.WM_USER, wParam, lParam);
        Globals.mousePointer.endDrag();
        return 0;
    }
}
