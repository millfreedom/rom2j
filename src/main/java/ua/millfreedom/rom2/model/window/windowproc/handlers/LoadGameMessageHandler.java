package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.visobj.LoadDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `LOAD_GAME` branch in CMainWindow::WindowProc @004852D8.
 */
public final class LoadGameMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private LoadGameMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.mSaveFile.filename = "";
        mainWindow.mSaveFile.title = "";
        mainWindow.pLoadDialogVisualObject = new LoadDialogVisualObject(1, 100, 0x1E, 600, 0x1C2, null, mainWindow.mSaveFile);
        mainWindow.showDialog(mainWindow.pLoadDialogVisualObject);
        return 1;
    }
}
