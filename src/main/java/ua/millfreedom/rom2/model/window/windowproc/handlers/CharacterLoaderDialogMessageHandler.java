package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.ScriptDataSupport;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SHOW_CHARACTER_LOADER_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class CharacterLoaderDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private CharacterLoaderDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        ScriptDataSupport.loadTownScriptData();
        mainWindow.serverLoopCounter = 0;
        mainWindow.sessionMode = CMainWindow.SESSION_MODE_MULTIPLAYER_CLIENT;
        mainWindow.showCharacterLoaderDialog();
        return 1;
    }
}
