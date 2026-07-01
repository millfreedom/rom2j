package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.ScriptDataSupport;
import ua.millfreedom.rom2.model.enums.SfxSounds;
import ua.millfreedom.rom2.model.sound.SoundManager;
import ua.millfreedom.rom2.model.visobj.MissionFailedHeaderDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;

/**
 * Native support extracted from the `SHOW_MISSION_FAILED_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowMissionFailedDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowMissionFailedDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Fully ported.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        if (mainWindow.missionFailureDialogShown == 0 && mainWindow.sessionMode == 2) {
            mainWindow.missionFailureDialogShown = 1;
            mainWindow.pMissionFailedHeaderDialogVisualObject = new MissionFailedHeaderDialogVisualObject(
                    1,
                    0x20,
                    100,
                    0x260,
                    0x17C,
                    resolveMissionFailedMessage(wParam)
            );
            mainWindow.showDialog(mainWindow.pMissionFailedHeaderDialogVisualObject);
            SoundManager.SFX_SOUNDS.get(SfxSounds.MFAILED.id)
                    .playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, (byte) 0xDC, 0);
        }
        return 1;
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8 g_failureReasons[wParam - 2] lookup.
     * Native `MissionScriptRuntime::applyDeadUnitFailureCounterCheck @00577600` can produce counter value `1`;
     * campaign failure text starts at `#failure2`, so Java maps that counter to the first loaded reason while
     * preserving the native `wParam - 2` lookup for explicit failure reason codes.
     */
    private static String resolveMissionFailedMessage(Object wParam) {
        int failureCode = CMainWindowWindowProcSupport.readMessageInt(wParam);
        int failureReasonIndex = failureCode == 1 ? 0 : failureCode - 2;
        return ScriptDataSupport.failureReasons.get(failureReasonIndex);
    }
}
