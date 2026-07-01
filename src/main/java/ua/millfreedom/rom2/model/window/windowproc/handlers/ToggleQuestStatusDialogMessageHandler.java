package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.visobj.CVisualObject;
import ua.millfreedom.rom2.model.visobj.QuestStatusDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `TOGGLE_QUEST_STATUS_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ToggleQuestStatusDialogMessageHandler {
    private static final int QUEST_STATUS_CHILD_ID = 0x12;

    /**
     * Java utility constructor.
     * not ported.
     */
    private ToggleQuestStatusDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        CVisualObject questStatusDialog = mainWindow.pMapVisualObject.getChildById(QUEST_STATUS_CHILD_ID);
        if (questStatusDialog == null) {
            mainWindow.pMapVisualObject.addChild(new QuestStatusDialogVisualObject(QUEST_STATUS_CHILD_ID, 10, 0x14, 0x172, 0xBC));
            mainWindow.pMapVisualObject.areaEffectRefreshPending = 1;
        } else if (CMainWindowWindowProcSupport.readMessageInt(wParam) == 0) {
            mainWindow.pMapVisualObject.removeChild(questStatusDialog);
            mainWindow.pMapVisualObject.areaEffectRefreshPending = 1;
        }
        return 1;
    }
}
