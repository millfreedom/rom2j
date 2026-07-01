package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.visobj.CVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `NOTIFY_DIALOG_CLOSED` branch in CMainWindow::WindowProc @004852D8.
 */
public final class NotifyDialogClosedMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private NotifyDialogClosedMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.onDialogClosed((CVisualObject) wParam);
        return 1;
    }
}
