package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native support extracted from the `SHOW_SHOP_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowShopDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowShopDialogMessageHandler() {
    }

    /**
     * Native support extracted from the `SHOW_SHOP_DIALOG` branch in CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (mainWindow.dialogsMask == 0
                || (mainWindow.sessionMode < CMainWindow.SESSION_MODE_CAMPAIGN
                && DialogsMaskFlag.isExactly(mainWindow.dialogsMask, GAMEPLAY))) {
            mainWindow.showShopDialog(CMainWindowWindowProcSupport.readMessageInt(wParam));
        }
        return 1;
    }
}
