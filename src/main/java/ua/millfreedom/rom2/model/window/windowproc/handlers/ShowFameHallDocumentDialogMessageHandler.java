package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native support extracted from the `SHOW_FAME_HALL_DOCUMENT_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowFameHallDocumentDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowFameHallDocumentDialogMessageHandler() {
    }

    /**
     * Native support extracted from the `SHOW_FAME_HALL_DOCUMENT_DIALOG` branch in CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (DialogsMaskFlag.isExactly(mainWindow.dialogsMask, GAMEPLAY)) {
            mainWindow.showFameHallDocumentDialog();
        }
        return 1;
    }
}
