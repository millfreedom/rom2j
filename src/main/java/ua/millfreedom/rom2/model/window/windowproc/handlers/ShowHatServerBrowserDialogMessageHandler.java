package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.visobj.HatServerBrowserDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

import static ua.millfreedom.rom2.model.enums.MessageCodes.HAT_SERVER_BROWSER_REFRESH_COMPLETE;

/**
 * Native support extracted from the `SHOW_HAT_SERVER_BROWSER_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowHatServerBrowserDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowHatServerBrowserDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.pHatServerBrowserDialogVisualObject = new HatServerBrowserDialogVisualObject(1, 0, -0x40, 0x280, 0x1E0);
        mainWindow.showDialog(mainWindow.pHatServerBrowserDialogVisualObject);
        int refreshState = HatServerBrowserDialogVisualObject.refreshHatServerRows(mainWindow.getHatIp());
        mainWindow.postMessage(HAT_SERVER_BROWSER_REFRESH_COMPLETE, refreshState, 0);
        return 1;
    }
}
