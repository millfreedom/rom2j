package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.text.GameTexts;
import ua.millfreedom.rom2.text.PatchText;

/**
 * Native support extracted from the `WRITE_CURRENT_MISSION_RESUME_SAVE` branch in CMainWindow::WindowProc @004852D8.
 */
public final class WriteCurrentMissionResumeSaveMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private WriteCurrentMissionResumeSaveMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        int menuAction = CMainWindowWindowProcSupport.readMessageInt(wParam);
        mainWindow.mSaveFile.title = menuAction == 0
                ? GameTexts.get(PatchText.RESTART_LAST_MISSION_55)
                : GameTexts.get(PatchText.ABORT_MISSION_AND_RETURN_TO_TOWN_95);
        mainWindow.mSaveFile.filename = "game%d.sav".formatted(9999 - menuAction);
        mainWindow.writeCurrentMissionResumeSave();
        mainWindow.m_LastRenderTime = Globals.currentTickMillis();
        mainWindow.m_FrameCounter = 0;
        mainWindow.m_LagAccumulator = 0;
        return 1;
    }
}
