package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.ScriptDataSupport;
import ua.millfreedom.rom2.model.visobj.CVisualObject;
import ua.millfreedom.rom2.model.visobj.TipsPromptDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SHOW_TIP_BY_ID` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowTipByIdMessageHandler {
    private static final int TIP_PROMPT_CHILD_ID = 0x10;

    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowTipByIdMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (Globals.gamePreferences.tipsMode != 0) {
            int tipId = CMainWindowWindowProcSupport.readMessageInt(wParam);
            String tipText = ScriptDataSupport.getTipText(tipId);
            CVisualObject existingTipPrompt = mainWindow.pMapVisualObject.getChildById(TIP_PROMPT_CHILD_ID);
            if (existingTipPrompt != null) {
                mainWindow.pMapVisualObject.removeChild(existingTipPrompt);
            }
            TipsPromptDialogVisualObject tipPrompt = new TipsPromptDialogVisualObject(
                    TIP_PROMPT_CHILD_ID,
                    10,
                    0x14,
                    0x172,
                    0xBC,
                    tipText
            );
            mainWindow.pMapVisualObject.addChild(tipPrompt);
        }
        return 1;
    }
}
