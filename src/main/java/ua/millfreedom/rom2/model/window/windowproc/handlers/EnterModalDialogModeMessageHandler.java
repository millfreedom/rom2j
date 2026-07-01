package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.CMousePointer;
import ua.millfreedom.rom2.model.window.CMainWindow;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.MODAL_DIALOG;

/**
 * Native support extracted from the `ENTER_MODAL_DIALOG_MODE` branch in CMainWindow::WindowProc @004852D8.
 */
public final class EnterModalDialogModeMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private EnterModalDialogModeMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.dialogsMask = MODAL_DIALOG.includeTo(mainWindow.dialogsMask);
        CMousePointer.Cursor_Default.setToMousePointer();
        return 1;
    }
}
