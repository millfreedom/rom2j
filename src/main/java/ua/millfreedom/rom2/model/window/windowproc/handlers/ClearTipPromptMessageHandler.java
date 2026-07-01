package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.CVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `CLEAR_TIP_PROMPT` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ClearTipPromptMessageHandler {
    private static final int TIP_PROMPT_CHILD_ID = 0x10;

    /**
     * Java utility constructor.
     * not ported.
     */
    private ClearTipPromptMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        CVisualObject tipPrompt = mainWindow.pMapVisualObject.getChildById(TIP_PROMPT_CHILD_ID);
        if (tipPrompt == null) {
            mainWindow.inputController.onMessage(MessageCodes.CLEAR_TIP_PROMPT, wParam, lParam);
        } else {
            mainWindow.pMapVisualObject.removeChild(tipPrompt);
        }
        return 1;
    }
}
