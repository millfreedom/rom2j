package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the grouped mouse-button branches in CMainWindow::WindowProc @004852D8.
 */
public final class MouseButtonMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private MouseButtonMessageHandler() {
    }

    /**
     * Native support extracted from the grouped `WM_LBUTTONDBLCLK` / right-button branches in
     * CMainWindow::WindowProc @004852D8.
     */
    public static int handleMouseButtonMessage(CMainWindow mainWindow, MessageCodes msg, Object wParam, Object lParam) {
        mainWindow.inputController.onMessage(msg, wParam, lParam);
        Globals.mousePointer.resetInputTimer();
        Globals.mousePointer.endDrag();
        return 0;
    }
}
