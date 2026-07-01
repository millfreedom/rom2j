package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.visobj.CenteredDialogVariantVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native support extracted from the `GAME_OPTIONS` branch in CMainWindow::WindowProc @004852D8.
 */
public final class GameOptionsMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private GameOptionsMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (DialogsMaskFlag.isExactly(mainWindow.dialogsMask, GAMEPLAY)) {
            mainWindow.showDialog(new CenteredDialogVariantVisualObject(1, 0x14, 0, 0x26C, 0x1E0));
        }
        return 1;
    }
}
