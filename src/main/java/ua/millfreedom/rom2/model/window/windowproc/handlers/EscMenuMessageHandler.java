package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.visobj.EscMenu421VisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native support extracted from the `ESC_MENU` branch in CMainWindow::WindowProc @004852D8.
 */
public final class EscMenuMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private EscMenuMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (DialogsMaskFlag.isExactly(mainWindow.dialogsMask, GAMEPLAY)) {
            mainWindow.showDialog(new EscMenu421VisualObject(
                    1,
                    100,
                    0x3C,
                    0x1B8,
                    400,
                    null,
                    0,
                    new CRect(0, 0, 0xF0, 0x28)
            ));
        }
        return 1;
    }
}
