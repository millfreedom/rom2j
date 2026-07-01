package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.visobj.ViewCutscenesHeaderDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.text.CutSceneText;
import ua.millfreedom.rom2.text.GameTexts;

import java.util.ArrayList;
import java.util.List;

/**
 * Native support extracted from the `VIEW_CUTSCENES` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ViewCutscenesMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ViewCutscenesMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (!Globals.videoResourcesAvailable) {
            mainWindow.postMessage(MessageCodes.SHOW_MAIN_MENU, 0, 0);
            return 1;
        }
        List<String> cutsceneTitles = new ArrayList<>();
        for (int cutsceneId = 0; cutsceneId < 0x20; cutsceneId++) {
            if ((Globals.usingVxD & (1 << (cutsceneId & 0x1F))) != 0) {
                cutsceneTitles.add(GameTexts.get(CutSceneText.byIndex(cutsceneId)));
            }
        }
        mainWindow.pViewCutscenesHeaderDialogVisualObject = new ViewCutscenesHeaderDialogVisualObject(
                0x11,
                100,
                0x1E,
                0x21C,
                0x1C2,
                cutsceneTitles
        );
        mainWindow.showDialog(mainWindow.pViewCutscenesHeaderDialogVisualObject);
        return 1;
    }
}
