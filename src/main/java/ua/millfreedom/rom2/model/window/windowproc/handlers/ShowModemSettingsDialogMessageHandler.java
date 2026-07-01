package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.visobj.ModemSettingsDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SHOW_MODEM_SETTINGS_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowModemSettingsDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowModemSettingsDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.showDialog(new ModemSettingsDialogVisualObject(1, 0, 100, 0x280, 400, mainWindow.PhoneBook));
        return 1;
    }
}
