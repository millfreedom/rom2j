package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.visobj.DiplomacySettingsDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native support extracted from the `DIPLOMACY` branch in CMainWindow::WindowProc @004852D8.
 */
public final class DiplomacyMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private DiplomacyMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (DialogsMaskFlag.isExactly(mainWindow.dialogsMask, GAMEPLAY)) {
            mainWindow.rebuildDiplomacy();
            mainWindow.pDiplomacySettingsDialogVisualObject = new DiplomacySettingsDialogVisualObject(
                    1,
                    10,
                    0,
                    0x276,
                    0x1E0,
                    mainWindow.m_Dilpomacy
            );
            mainWindow.showDialog(mainWindow.pDiplomacySettingsDialogVisualObject);
        }
        return 1;
    }
}
