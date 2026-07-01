package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.TcpIpSettingsHeaderDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SHOW_TCP_IP_SETTINGS_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowTcpIpSettingsDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowTcpIpSettingsDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Fully ported. Preserves dialog construction and native command-line/server-config auto-submit posts.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.showDialog(new TcpIpSettingsHeaderDialogVisualObject(1, 0, 100, 0x280, 300, mainWindow.lastIP));
        if (Globals.commandLine.contains("-protocol")) {
            mainWindow.postMessage(MessageCodes.DIALOG_OK, 0, 0);
        }
        if (Globals.serverConfig.protocol >= 0) {
            mainWindow.postMessage(MessageCodes.DIALOG_OK, 0, 0);
        }
        return 1;
    }
}
