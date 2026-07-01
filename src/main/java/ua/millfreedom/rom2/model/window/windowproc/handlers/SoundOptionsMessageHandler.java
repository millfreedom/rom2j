package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.SoundPreferencesDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native support extracted from the `SOUND_OPTIONS` branch in CMainWindow::WindowProc @004852D8.
 */
public final class SoundOptionsMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private SoundOptionsMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        int modeFlags = mainWindow.dialogsMask;
        if (DialogsMaskFlag.isExactly(modeFlags, GAMEPLAY)
                || (modeFlags == 0 && Globals.gameServer.isServerLifecycleAllocated())) {
            mainWindow.showDialog(new SoundPreferencesDialogVisualObject(
                    1,
                    100,
                    0x1E,
                    0x280,
                    0x1C2,
                    null,
                    mainWindow.getSoundPreferences()
            ));
        } else if (modeFlags == 0) {
            mainWindow.postMessage(MessageCodes.SHOW_HAT_SERVER_LIST_DIALOG, 0, 0);
        }
        return 1;
    }
}
