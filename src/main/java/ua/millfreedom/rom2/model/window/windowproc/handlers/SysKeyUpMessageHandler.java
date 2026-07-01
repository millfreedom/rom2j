package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;

/**
 * Java support for WM_SYSKEYUP routing through CMainWindow::OnSysKeyUp @00485291.
 */
public final class SysKeyUpMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private SysKeyUpMessageHandler() {
    }

    /**
     * Java support for sys-key-up delivery to CMainWindow::OnSysKeyUp @00485291.
     * Native CMainWindow::WindowProc @004852D8 routes WM_SYSKEYUP through the MFC message-map handler.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.onSysKeyUp(readMessageInt(wParam), 0, readMessageInt(lParam));
        return 0;
    }
}
