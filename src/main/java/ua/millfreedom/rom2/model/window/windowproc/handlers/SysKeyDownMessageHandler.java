package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;

/**
 * Java support for WM_SYSKEYDOWN routing through CMainWindow::OnSysKeyDown @0048509B.
 */
public final class SysKeyDownMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private SysKeyDownMessageHandler() {
    }

    /**
     * Java support for GLFW sys-key delivery to CMainWindow::OnSysKeyDown @0048509B.
     * Native CMainWindow::WindowProc @004852D8 routes WM_SYSKEYDOWN through the MFC message-map handler.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.onSysKeyDown(readMessageInt(wParam), 0, readMessageInt(lParam));
        return 0;
    }
}
