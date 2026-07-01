package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

import static ua.millfreedom.rom2.model.window.windowproc.handlers.CMainWindowWindowProcSupport.readMessageInt;

/**
 * Java support for WM_KEYDOWN routing through CMainWindow::OnKeyDown @00484A76.
 * Native CMainWindow::WindowProc @004852D8 consumes this message before the default route.
 */
public final class KeyDownMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private KeyDownMessageHandler() {
    }

    /**
     * Java support for GLFW key-down delivery to CMainWindow::OnKeyDown @00484A76.
     * Native CMainWindow::WindowProc @004852D8 routes WM_KEYDOWN through the MFC message-map handler.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.onKeyDown(readMessageInt(wParam), 0, readMessageInt(lParam));
        return 0;
    }
}
