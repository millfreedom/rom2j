package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Java support for WM_SETFOCUS routing through CMainWindow::OnSetFocus @00484A46.
 */
public final class SetFocusMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private SetFocusMessageHandler() {
    }

    /**
     * Java support for focus delivery to CMainWindow::OnSetFocus @00484A46.
     * Native CMainWindow::WindowProc @004852D8 routes WM_SETFOCUS through the MFC message-map handler.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.onSetFocus();
        return 0;
    }
}
