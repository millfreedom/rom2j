package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Java support for WM_SETCURSOR routing through CMainWindow::OnSetCursor @00484A09.
 */
public final class SetCursorMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private SetCursorMessageHandler() {
    }

    /**
     * Java support for cursor-message delivery to CMainWindow::OnSetCursor @00484A09.
     * Native CMainWindow::WindowProc @004852D8 routes WM_SETCURSOR through the MFC message-map handler.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        return mainWindow.onSetCursor();
    }
}
