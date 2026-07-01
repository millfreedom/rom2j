package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.visobj.HatServerListDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SHOW_HAT_SERVER_LIST_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowHatServerListDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowHatServerListDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.showDialog(new HatServerListDialogVisualObject(1, 0, 100, 0x280, 300, mainWindow.Hat));
        return 1;
    }
}
