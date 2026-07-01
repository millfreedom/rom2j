package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.visobj.SerialSettingsDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SHOW_SERIAL_SETTINGS_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowSerialSettingsDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowSerialSettingsDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.showDialog(new SerialSettingsDialogVisualObject(
                1,
                100,
                0,
                0x21C,
                0x1E0,
                mainWindow.serialSettings
        ));
        return 1;
    }
}
