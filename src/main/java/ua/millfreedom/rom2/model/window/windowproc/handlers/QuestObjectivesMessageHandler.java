package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.model.visobj.QuestObjectivesHeaderDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.window.DialogsMaskFlag;

import static ua.millfreedom.rom2.model.window.DialogsMaskFlag.GAMEPLAY;

/**
 * Native support extracted from the `QUEST_OBJECTIVES` branch in CMainWindow::WindowProc @004852D8.
 */
public final class QuestObjectivesMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private QuestObjectivesMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (DialogsMaskFlag.isExactly(mainWindow.dialogsMask, GAMEPLAY)) {
            mainWindow.showDialog(new QuestObjectivesHeaderDialogVisualObject(
                    1,
                    0x14,
                    0,
                    0x26C,
                    500
            ));
        }
        return 1;
    }
}
