package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from CMainWindow::MSGMAP_ENTRY_ARRAY WM_SIZE dispatch to CMainWindow::OnSize @004926CA.
 */
public final class SizeMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private SizeMessageHandler() {
    }

    /**
     * Native support extracted from WM_SIZE message parameter dispatch to CMainWindow::OnSize @004926CA.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        int sizeType = CMainWindowWindowProcSupport.readMessageInt(wParam);
        int packedSize = CMainWindowWindowProcSupport.readMessageInt(lParam);
        int cx = packedSize & 0xFFFF;
        int cy = (packedSize >>> 16) & 0xFFFF;
        mainWindow.onSize(sizeType, cx, cy);
        return 1;
    }
}
