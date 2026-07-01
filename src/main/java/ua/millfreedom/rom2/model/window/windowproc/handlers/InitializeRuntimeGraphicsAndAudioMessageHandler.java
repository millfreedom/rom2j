package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `INITIALIZE_RUNTIME_GRAPHICS_AND_AUDIO` branch in CMainWindow::WindowProc @004852D8.
 */
public final class InitializeRuntimeGraphicsAndAudioMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private InitializeRuntimeGraphicsAndAudioMessageHandler() {
    }

    /**
     * Native support extracted from the `INITIALIZE_RUNTIME_GRAPHICS_AND_AUDIO` branch in CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        mainWindow.initializeRuntimeGraphicsAndAudio();
        return 1;
    }
}
