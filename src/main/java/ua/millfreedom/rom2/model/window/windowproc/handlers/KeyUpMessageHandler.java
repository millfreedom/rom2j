package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;

import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;

/**
 * Java support for WM_KEYUP routing through CMainWindow::OnKeyUp @0048521C.
 */
public final class KeyUpMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private KeyUpMessageHandler() {
    }

    /**
     * Native support extracted from the `WM_KEYUP` direct branch in CMainWindow::WindowProc @004852D8,
     * followed by the CMainWindow::OnKeyUp @0048521C message-map handler.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.inputController.onMessage(MessageCodes.WM_KEYUP, wParam, lParam);
        mainWindow.onKeyUp(readMessageInt(wParam), 0, readMessageInt(lParam));
        return 0;
    }
}
