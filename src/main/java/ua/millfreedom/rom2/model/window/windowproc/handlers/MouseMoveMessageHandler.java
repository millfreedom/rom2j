package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `WM_MOUSEMOVE` branch in CMainWindow::WindowProc @004852D8.
 */
public final class MouseMoveMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private MouseMoveMessageHandler() {
    }

    /**
     * Native support extracted from the `WM_MOUSEMOVE` branch in CMainWindow::WindowProc @004852D8.
     * Partial port. Java refreshes the software cursor on the handled path instead of calling the native OS
     * `SetCursorPos`.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        int handledResult = mainWindow.inputController.onMessage(MessageCodes.WM_MOUSEMOVE, wParam, lParam);
        if (handledResult != 0) {
            Globals.renderer.refreshMousePointer();
        }
        Globals.mousePointer.resetInputTimer();
        return 0;
    }
}
