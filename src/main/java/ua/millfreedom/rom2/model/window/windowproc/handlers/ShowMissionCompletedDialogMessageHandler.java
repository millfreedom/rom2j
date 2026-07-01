package ua.millfreedom.rom2.model.window.windowproc.handlers;

import ua.millfreedom.rom2.Globals;
import ua.millfreedom.rom2.model.CRect;
import ua.millfreedom.rom2.model.enums.MessageCodes;
import ua.millfreedom.rom2.model.enums.SfxSounds;
import ua.millfreedom.rom2.model.sound.SoundManager;
import ua.millfreedom.rom2.model.visobj.TwoActionMenuListDialogVisualObject;
import ua.millfreedom.rom2.model.window.CMainWindow;
import ua.millfreedom.rom2.model.world.ScenarioProgressSupport;
import ua.millfreedom.rom2.text.GameTexts;
import ua.millfreedom.rom2.text.MainText;

/**
 * Native support extracted from the `SHOW_MISSION_COMPLETED_DIALOG` branch in CMainWindow::WindowProc @004852D8.
 */
public final class ShowMissionCompletedDialogMessageHandler {
    /**
     * Java utility constructor.
     * not ported.
     */
    private ShowMissionCompletedDialogMessageHandler() {
    }

    /**
     * Native support extracted from CMainWindow::WindowProc @004852D8.
     * Partial port. Preserves the recovered campaign-ending EXIT_MAP split, mission-completed dialog construction, and
     * MCOMPLET playback gate.
     */
    public static int handle(CMainWindow mainWindow, Object wParam, Object lParam) {
        boolean campaignEndingReached = ScenarioProgressSupport.isCampaignEndingReached();
        mainWindow.completedMissionExitPending = 1;
        if (campaignEndingReached) {
            mainWindow.postMessage(MessageCodes.EXIT_MAP, 0, 0);
            return 1;
        }
        int dialogWidth = 0x200 - 0x80;
        int dialogHeight = 0x17C - 200;
        mainWindow.pTwoActionMenuListDialogVisualObject = new TwoActionMenuListDialogVisualObject(1,
                0x80, 200,
                0x200, 0x17C,
                GameTexts.get(MainText.MISSION_COMPLETED_140),
                new CRect(dialogWidth / 8, dialogHeight - 0x78,
                        (dialogWidth * 7) / 8, dialogHeight - 0x60)
        );
        mainWindow.showDialog(mainWindow.pTwoActionMenuListDialogVisualObject);
        SoundManager.SFX_SOUNDS.get(SfxSounds.MCOMPLET.id)
                .playIfNotPlaying(Globals.soundPreferences.sfxVolume, false, (byte) 0xDC, 0);
        return 1;
    }
}
