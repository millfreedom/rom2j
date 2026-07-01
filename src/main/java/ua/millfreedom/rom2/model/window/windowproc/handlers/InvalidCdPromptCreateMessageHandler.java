package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.HeaderDialogVariantVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.text.GameTexts;
import ua.millfreedom.rom2.text.MainText;

/**
 * Native support extracted from the `SHOW_INVALID_CD_PROMPT_CREATE` branch in CMainWindow::WindowProc @004852D8.
 */
public final class InvalidCdPromptCreateMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private InvalidCdPromptCreateMessageHandler() {
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
                        0x17C,
                        GameTexts.get(MainText.VALUE_4_181),
                        null,
                        1
                )
        );
        if (promptResult == MessageCodes.DIALOG_OK) {
            mainWindow.postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
        } else {
            mainWindow.postMessage(MessageCodes.WM_CLOSE, 0, 0);
        }
        return 1;
    }
}
