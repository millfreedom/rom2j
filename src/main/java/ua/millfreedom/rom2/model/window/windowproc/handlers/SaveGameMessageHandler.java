package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.visobj.SaveDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native support extracted from the `SAVE_GAME` branch in CMainWindow::WindowProc @004852D8.
 */
public final class SaveGameMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private SaveGameMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (DialogsMaskFlag.isExactly(mainWindow.dialogsMask, GAMEPLAY) || mainWindow.dialogsMask == 0) {
            mainWindow.mSaveFile.filename = "";
            mainWindow.mSaveFile.title = "";
            mainWindow.pSaveDialogVisualObject = new SaveDialogVisualObject(1, 100, 0x1E, 600, 0x1C2, null, mainWindow.mSaveFile);
            mainWindow.showDialog(mainWindow.pSaveDialogVisualObject);
        }
        return 1;
    }
}
