package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.visobj.HeaderDialogVariantVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native support extracted from the `SHOW_HELP_TEXT_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowHelpTextDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowHelpTextDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8 for message `0x0434`.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (DialogsMaskFlag.isExactly(mainWindow.dialogsMask, GAMEPLAY)) {
            mainWindow.showDialog(new HeaderDialogVariantVisualObject(
                    1,
                    0x20,
                    0x30,
                    0x260,
                    0x1B0,
                    Globals.helpText,
                    null,
                    0
            ));
        }
        return 1;
    }
}
