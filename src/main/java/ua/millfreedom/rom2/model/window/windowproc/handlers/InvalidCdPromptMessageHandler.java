package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.HeaderDialogVariantVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.text.GameTexts;
import ua.millfreedom.rom2.text.MainText;

/**
 * Native support extracted from the `SHOW_INVALID_CD_PROMPT` branch in CMainWindow::WindowProc @004852D8.
 */
public final class InvalidCdPromptMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private InvalidCdPromptMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        MessageCodes promptResult = mainWindow.showDialogAndAwaitResult(
                new HeaderDialogVariantVisualObject(
                        1,
                        100,
                        100,
                        0x21C,
                        0x1E0,
                        GameTexts.get(MainText.INCORRECT_CD_OR_NO_CD_DETECTED_CLICK_CANCEL_INSERT_CD_AND_START_151),
                        null,
                        1
                )
        );
        if (promptResult == MessageCodes.DIALOG_OK) {
            Globals.mainMenuDisabledButtonMask = 0x35;
            mainWindow.postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
        } else {
            mainWindow.postMessage(MessageCodes.WM_CLOSE, 0, 0);
        }
        return 1;
    }
}
